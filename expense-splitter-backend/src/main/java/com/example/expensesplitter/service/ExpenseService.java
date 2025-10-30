package com.example.expensesplitter.service;

import com.example.expensesplitter.dto.BalanceDto;
import com.example.expensesplitter.dto.CreateExpenseRequest;
import com.example.expensesplitter.entity.Expense;
import com.example.expensesplitter.entity.ExpenseShare;
import com.example.expensesplitter.entity.Group;
import com.example.expensesplitter.entity.Membership;
import com.example.expensesplitter.entity.User;
import com.example.expensesplitter.repository.ExpenseRepository;
import com.example.expensesplitter.repository.ExpenseShareRepository;
import com.example.expensesplitter.repository.GroupRepository;
import com.example.expensesplitter.repository.MembershipRepository;
import com.example.expensesplitter.repository.UserRepository;
import com.example.expensesplitter.util.SplitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.expensesplitter.repository.SettlementRepository settlementRepository;

    // ---------------- CREATE EXPENSE ----------------
    @Transactional
    public Expense createExpense(Long groupId, User creator, CreateExpenseRequest req) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        List<Membership> memberships = membershipRepository.findByGroup_Id(group.getId());
        if (memberships == null || memberships.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group has no members");
        }

        List<Long> memberUserIds = memberships.stream()
                .map(Membership::getUser)
                .filter(Objects::nonNull)
                .map(User::getId)
                .collect(Collectors.toList());

        if (!memberUserIds.contains(creator.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this group");
        }

        User payer = userRepository.findById(req.getPayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payer not found"));
        if (!memberUserIds.contains(payer.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payer is not a member of this group");
        }

        BigDecimal amount = req.getAmount();
        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is required");
        }
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);

        long totalCents = SplitUtil.toCents(amount);

        String splitType = req.getSplitType() == null ? "EQUAL" : req.getSplitType().trim().toUpperCase(Locale.ROOT);
        Map<Long, Long> allocationInCents;

        switch (splitType) {
            case "EQUAL":
                allocationInCents = SplitUtil.allocateEqual(totalCents, memberUserIds);
                break;

            case "PERCENT":
                if (req.getShares() == null || req.getShares().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shares required for PERCENT split");
                }
                Map<Long, BigDecimal> percentMap = new LinkedHashMap<>();
                for (CreateExpenseRequest.ShareLine sl : req.getShares()) {
                    if (sl.getUserId() == null || sl.getPercent() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Each share line must include userId and percent for PERCENT split");
                    }
                    percentMap.put(sl.getUserId(), sl.getPercent());
                }
                BigDecimal totalPercent = percentMap.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2, RoundingMode.HALF_EVEN);
                if (totalPercent.compareTo(new BigDecimal("100.00")) != 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percents must sum to exactly 100.00");
                }
                allocationInCents = SplitUtil.allocatePercent(totalCents, percentMap);
                break;

            case "CUSTOM":
                if (req.getShares() == null || req.getShares().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shares required for CUSTOM split");
                }
                Map<Long, BigDecimal> amountMap = new LinkedHashMap<>();
                for (CreateExpenseRequest.ShareLine sl : req.getShares()) {
                    if (sl.getUserId() == null || sl.getAmount() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Each share line must include userId and amount for CUSTOM split");
                    }
                    amountMap.put(sl.getUserId(), sl.getAmount().setScale(2, RoundingMode.HALF_EVEN));
                }
                allocationInCents = SplitUtil.allocateCustom(totalCents, amountMap);
                break;

            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown splitType: " + splitType);
        }

        Expense expense = new Expense();
        expense.setGroup(group);
        expense.setCreatedBy(creator);
        expense.setPayer(payer);
        expense.setAmount(amount);
        expense.setCurrency(req.getCurrency() == null ? "INR" : req.getCurrency());
        expense.setSplitType(splitType);
        expense.setNote(req.getNote());

        for (Map.Entry<Long, Long> entry : allocationInCents.entrySet()) {
            Long uid = entry.getKey();
            long cents = entry.getValue();

            if (!memberUserIds.contains(uid)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Shared user is not a member of the group: " + uid);
            }
            User u = userRepository.findById(uid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "User not found in shares: " + uid));

            ExpenseShare share = new ExpenseShare();
            share.setUser(u);
            share.setShareAmount(SplitUtil.centsToBigDecimal(cents));
            expense.addShare(share);
        }

        return expenseRepository.save(expense);
    }

    // ---------------- LIST EXPENSES ----------------
    public List<com.example.expensesplitter.dto.ExpenseDto> listGroupExpenses(Long groupId, User requester) {
        List<Membership> memberships = membershipRepository.findByGroup_Id(groupId);
        if (memberships == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found or no memberships");
        }
        List<Long> memberUserIds = memberships.stream()
                .map(Membership::getUser)
                .filter(Objects::nonNull)
                .map(User::getId)
                .collect(Collectors.toList());

        if (!memberUserIds.contains(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a group member");
        }

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        List<com.example.expensesplitter.dto.ExpenseDto> dtos = new ArrayList<>();
        for (Expense e : expenses) {
            com.example.expensesplitter.dto.ExpenseDto dto = new com.example.expensesplitter.dto.ExpenseDto();
            dto.setId(e.getId());
            dto.setGroupId(e.getGroup().getId());
            dto.setCreatedById(e.getCreatedBy().getId());
            dto.setPayerId(e.getPayer().getId());
            dto.setAmount(e.getAmount());
            dto.setCurrency(e.getCurrency());
            dto.setSplitType(e.getSplitType());
            dto.setNote(e.getNote());
            dto.setCreatedAt(e.getCreatedAt());

            List<com.example.expensesplitter.dto.ExpenseDto.ShareDto> shareDtos = new ArrayList<>();
            for (ExpenseShare s : e.getShares()) {
                com.example.expensesplitter.dto.ExpenseDto.ShareDto sd = new com.example.expensesplitter.dto.ExpenseDto.ShareDto();
                sd.setUserId(s.getUser().getId());
                sd.setShareAmount(s.getShareAmount());
                sd.setSettled(s.isSettled());
                shareDtos.add(sd);
            }
            dto.setShares(shareDtos);
            dtos.add(dto);
        }
        return dtos;
    }

    // ---------------- COMPUTE BALANCES ----------------
    public List<BalanceDto> computeBalances(Long groupId, User requester) {
        log.info("Computing balances for group {} by user {}", groupId, requester.getId());

        List<Membership> memberships = membershipRepository.findByGroup_Id(groupId);
        if (memberships == null || memberships.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found or has no members");
        }

        List<Long> memberUserIds = memberships.stream()
                .map(m -> m.getUser().getId())
                .toList();

        if (!memberUserIds.contains(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a group member");
        }

        Map<Long, BigDecimal> balances = new HashMap<>();
        for (Membership m : memberships) {
            balances.put(m.getUser().getId(), BigDecimal.ZERO);
        }

        // Expenses
        List<Expense> expenses = Optional.ofNullable(expenseRepository.findByGroupId(groupId)).orElse(Collections.emptyList());
        for (Expense expense : expenses) {
            if (expense.getPayer() == null) continue; // defensive
            Long payerId = expense.getPayer().getId();
            BigDecimal amount = expense.getAmount() != null ? expense.getAmount() : BigDecimal.ZERO;
            balances.put(payerId, balances.getOrDefault(payerId, BigDecimal.ZERO).add(amount));

            // NOTE: expense.getShares() is a Set<ExpenseShare> in your domain; use emptySet() here
            for (ExpenseShare share : Optional.ofNullable(expense.getShares()).orElse(Collections.emptySet())) {
                if (share.getUser() == null) continue;
                Long uid = share.getUser().getId();
                BigDecimal shareAmt = share.getShareAmount() != null ? share.getShareAmount() : BigDecimal.ZERO;
                balances.put(uid, balances.getOrDefault(uid, BigDecimal.ZERO).subtract(shareAmt));
            }
        }

        // Settlements
        List<com.example.expensesplitter.entity.Settlement> settlements =
                Optional.ofNullable(settlementRepository.findByGroup_Id(groupId)).orElse(Collections.emptyList());
        for (com.example.expensesplitter.entity.Settlement s : settlements) {
            if (s.getFromUser() == null || s.getToUser() == null) continue;
            Long from = s.getFromUser().getId();
            Long to = s.getToUser().getId();
            BigDecimal amt = s.getAmount() != null ? s.getAmount() : BigDecimal.ZERO;
            balances.put(from, balances.getOrDefault(from, BigDecimal.ZERO).subtract(amt));
            balances.put(to, balances.getOrDefault(to, BigDecimal.ZERO).add(amt));
        }

        List<BalanceDto> result = new ArrayList<>();
        for (Membership m : memberships) {
            Long uid = m.getUser().getId();
            String name = m.getUser().getName();
            BigDecimal net = balances.getOrDefault(uid, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_EVEN);
            result.add(new BalanceDto(uid, name, net));
        }

        log.info("Computed balances: {}", result);
        return result;
    }
}

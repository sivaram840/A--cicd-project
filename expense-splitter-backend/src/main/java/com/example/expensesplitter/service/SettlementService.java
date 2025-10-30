package com.example.expensesplitter.service;

import com.example.expensesplitter.dto.RecordSettlementRequest;
import com.example.expensesplitter.dto.SettlementDto;
import com.example.expensesplitter.entity.Group;
import com.example.expensesplitter.entity.Membership;
import com.example.expensesplitter.entity.Settlement;
import com.example.expensesplitter.entity.User;
import com.example.expensesplitter.repository.GroupRepository;
import com.example.expensesplitter.repository.MembershipRepository;
import com.example.expensesplitter.repository.SettlementRepository;
import com.example.expensesplitter.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public SettlementService(SettlementRepository settlementRepository,
                             GroupRepository groupRepository,
                             UserRepository userRepository,
                             MembershipRepository membershipRepository) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public SettlementDto recordSettlement(Long groupId, User recorder, RecordSettlementRequest req) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // Ensure recorder is a member
        membershipRepository.findByGroupAndUser(group, recorder)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a group member"));

        // validate from/to users exist and are members
        User from = userRepository.findById(req.getFromUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "From user not found"));
        User to = userRepository.findById(req.getToUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "To user not found"));

        membershipRepository.findByGroupAndUser(group, from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "From user not a member"));
        membershipRepository.findByGroupAndUser(group, to)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "To user not a member"));

        if (req.getAmount() == null || req.getAmount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        Settlement s = new Settlement();
        s.setGroup(group);
        s.setFromUser(from);
        s.setToUser(to);
        s.setAmount(req.getAmount().setScale(2, java.math.RoundingMode.HALF_EVEN));
        s.setCurrency(req.getCurrency() == null ? "INR" : req.getCurrency());
        s.setNote(req.getNote());
        s.setRecordedBy(recorder);

        Settlement saved = settlementRepository.save(s);

        return new SettlementDto(
                saved.getId(),
                saved.getFromUser().getId(),
                saved.getFromUser().getName(),
                saved.getToUser().getId(),
                saved.getToUser().getName(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getNote(),
                saved.getRecordedBy().getId(),
                saved.getRecordedBy().getName(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<SettlementDto> listSettlements(Long groupId, User requester) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        membershipRepository.findByGroupAndUser(group, requester)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a group member"));

        List<Settlement> settlements = settlementRepository.findByGroup_Id(groupId);
        return settlements.stream().map(s -> new SettlementDto(
                s.getId(),
                s.getFromUser().getId(),
                s.getFromUser().getName(),
                s.getToUser().getId(),
                s.getToUser().getName(),
                s.getAmount(),
                s.getCurrency(),
                s.getNote(),
                s.getRecordedBy().getId(),
                s.getRecordedBy().getName(),
                s.getCreatedAt()
        )).collect(Collectors.toList());
    }
}


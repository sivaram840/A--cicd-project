package com.example.expensesplitter.controller;

import com.example.expensesplitter.dto.CreateExpenseRequest;
import com.example.expensesplitter.dto.ExpenseDto;
import com.example.expensesplitter.entity.Expense;
import com.example.expensesplitter.entity.User;
import com.example.expensesplitter.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.expensesplitter.security.CurrentUser;
import com.example.expensesplitter.dto.BalanceDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;



import java.util.List;

/**
 * ExpenseController - endpoints to create and list expenses for a group.
 *
 * NOTE: This assumes your @CurrentUser resolver injects the JPA User entity.
 * If it injects a principal object, adapt the parameter and fetch the User entity from DB.
 */
@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(
            @PathVariable("groupId") Long groupId,
            @Valid @RequestBody CreateExpenseRequest req,
            @CurrentUser User currentUser
    ) {
        Expense saved = expenseService.createExpense(groupId, currentUser, req);

        ExpenseDto dto = new ExpenseDto();
        dto.setId(saved.getId());
        dto.setGroupId(saved.getGroup().getId());
        dto.setCreatedById(saved.getCreatedBy().getId());
        dto.setPayerId(saved.getPayer().getId());
        dto.setAmount(saved.getAmount());
        dto.setCurrency(saved.getCurrency());
        dto.setSplitType(saved.getSplitType());
        dto.setNote(saved.getNote());
        dto.setCreatedAt(saved.getCreatedAt());

        List<ExpenseDto.ShareDto> shareDtos = new java.util.ArrayList<>();
        saved.getShares().forEach(s -> {
            ExpenseDto.ShareDto sd = new ExpenseDto.ShareDto();
            sd.setUserId(s.getUser().getId());
            sd.setShareAmount(s.getShareAmount());
            sd.setSettled(s.isSettled());
            shareDtos.add(sd);
        });
        dto.setShares(shareDtos);

        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> listExpenses(
            @PathVariable("groupId") Long groupId,
            @CurrentUser User currentUser
    ) {
        List<ExpenseDto> list = expenseService.listGroupExpenses(groupId, currentUser);
        return ResponseEntity.ok(list);
    }

    /**
     * Get net balances for all members in the group.
     * Only accessible if the requester is a member.
     */
    @GetMapping("/balances")
    public ResponseEntity<?> getGroupBalances(@PathVariable("groupId") Long groupId,
                                              @CurrentUser User requester) {
        try {
            List<BalanceDto> balances = expenseService.computeBalances(groupId, requester);
            return ResponseEntity.ok(balances);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error: " + e.getMessage());
        }
    }

}

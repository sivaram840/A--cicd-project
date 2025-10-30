package com.example.expensesplitter.controller;

import com.example.expensesplitter.dto.RecordSettlementRequest;
import com.example.expensesplitter.dto.SettlementDto;
import com.example.expensesplitter.entity.User;
import com.example.expensesplitter.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.expensesplitter.security.CurrentUser;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    public ResponseEntity<?> recordSettlement(@PathVariable("groupId") Long groupId,
                                              @RequestBody RecordSettlementRequest req,
                                              @CurrentUser User currentUser) {
        try {
            SettlementDto dto = settlementService.recordSettlement(groupId, currentUser, req);
            return ResponseEntity.ok(dto);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listSettlements(@PathVariable("groupId") Long groupId,
                                             @CurrentUser User currentUser) {
        try {
            List<SettlementDto> list = settlementService.listSettlements(groupId, currentUser);
            return ResponseEntity.ok(list);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}


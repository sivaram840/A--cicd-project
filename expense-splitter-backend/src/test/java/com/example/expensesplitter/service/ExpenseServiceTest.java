package com.example.expensesplitter.service;

import com.example.expensesplitter.dto.CreateExpenseRequest;
import com.example.expensesplitter.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExpenseServiceTest {

    // Directly test SplitUtil logic (cheapest unit test coverage)
    @Test
    void testEqualSplit() {
        long total = 1000; // ₹10.00
        List<Long> members = List.of(1L, 2L, 3L);
        Map<Long, Long> result = com.example.expensesplitter.util.SplitUtil.allocateEqual(total, members);

        assertEquals(3, result.size());
        long sum = result.values().stream().mapToLong(Long::longValue).sum();
        assertEquals(total, sum);
    }

    @Test
    void testPercentSplit() {
        long total = 1000; // ₹10
        Map<Long, BigDecimal> percents = Map.of(
                1L, new BigDecimal("50"),
                2L, new BigDecimal("50")
        );
        Map<Long, Long> result = com.example.expensesplitter.util.SplitUtil.allocatePercent(total, percents);
        assertEquals(1000, result.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void testCustomSplit() {
        long total = 1000;
        Map<Long, BigDecimal> amounts = Map.of(
                1L, new BigDecimal("4.00"),
                2L, new BigDecimal("6.00")
        );
        Map<Long, Long> result = com.example.expensesplitter.util.SplitUtil.allocateCustom(total, amounts);
        assertEquals(total, result.values().stream().mapToLong(Long::longValue).sum());
    }
}

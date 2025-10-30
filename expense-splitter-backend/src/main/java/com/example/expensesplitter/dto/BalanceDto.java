package com.example.expensesplitter.dto;

import java.math.BigDecimal;

/**
 * BalanceDto - represents a member's net balance in a group.
 * netBalance: positive = this user should receive money;
 *             negative = this user owes money.
 */
public class BalanceDto {
    private Long userId;
    private String name;
    private BigDecimal netBalance;

    public BalanceDto() {}

    public BalanceDto(Long userId, String name, BigDecimal netBalance) {
        this.userId = userId;
        this.name = name;
        this.netBalance = netBalance;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getNetBalance() { return netBalance; }
    public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }
}


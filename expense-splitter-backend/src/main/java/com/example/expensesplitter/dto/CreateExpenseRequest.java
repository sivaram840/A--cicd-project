package com.example.expensesplitter.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class CreateExpenseRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "PayerId is required")
    private Long payerId;

    @NotBlank(message = "Split type is required") // ensures non-null and non-empty
    private String splitType; // "EQUAL", "PERCENT", "CUSTOM"

    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    private String currency;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;

    private List<ShareLine> shares; // For PERCENT or CUSTOM: userId + percent or amount

    public static class ShareLine {
        @NotNull(message = "UserId is required in each share line")
        private Long userId;

        // Optional depending on splitType, so no direct validation here
        private BigDecimal percent; // used when splitType == "PERCENT"
        private BigDecimal amount;  // used when splitType == "CUSTOM"

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public BigDecimal getPercent() { return percent; }
        public void setPercent(BigDecimal percent) { this.percent = percent; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    // getters / setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getPayerId() { return payerId; }
    public void setPayerId(Long payerId) { this.payerId = payerId; }

    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<ShareLine> getShares() { return shares; }
    public void setShares(List<ShareLine> shares) { this.shares = shares; }
}

package com.example.expensesplitter.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ExpenseDto {
    private Long id;
    private Long groupId;
    private Long createdById;
    private Long payerId;
    private BigDecimal amount;
    private String currency;
    private String splitType;
    private String note;
    private Instant createdAt;
    private List<ShareDto> shares;

    public static class ShareDto {
        private Long userId;
        private BigDecimal shareAmount;
        private boolean isSettled;
        // getters/setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public BigDecimal getShareAmount() { return shareAmount; }
        public void setShareAmount(BigDecimal shareAmount) { this.shareAmount = shareAmount; }
        public boolean isSettled() { return isSettled; }
        public void setSettled(boolean settled) { isSettled = settled; }
    }

    // getters/setters (omitted for brevity — add all)
    // ... generate them or use your IDE to create
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public Long getPayerId() { return payerId; }
    public void setPayerId(Long payerId) { this.payerId = payerId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<ShareDto> getShares() { return shares; }
    public void setShares(List<ShareDto> shares) { this.shares = shares; }
}

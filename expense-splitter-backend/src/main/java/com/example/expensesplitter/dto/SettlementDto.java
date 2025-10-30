package com.example.expensesplitter.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class SettlementDto {
    private Long id;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private BigDecimal amount;
    private String currency;
    private String note;
    private Long recordedById;
    private String recordedByName;
    private Instant createdAt;

    public SettlementDto() {}

    public SettlementDto(Long id, Long fromUserId, String fromUserName, Long toUserId, String toUserName,
                         BigDecimal amount, String currency, String note,
                         Long recordedById, String recordedByName, Instant createdAt) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.toUserId = toUserId;
        this.toUserName = toUserName;
        this.amount = amount;
        this.currency = currency;
        this.note = note;
        this.recordedById = recordedById;
        this.recordedByName = recordedByName;
        this.createdAt = createdAt;
    }

    // getters/setters omitted for brevity in this message — implement standard getters and setters
    // (Copy the pattern from BalanceDto)
    // ...
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getFromUserId(){return fromUserId;} public void setFromUserId(Long v){this.fromUserId=v;}
    public String getFromUserName(){return fromUserName;} public void setFromUserName(String v){this.fromUserName=v;}
    public Long getToUserId(){return toUserId;} public void setToUserId(Long v){this.toUserId=v;}
    public String getToUserName(){return toUserName;} public void setToUserName(String v){this.toUserName=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){this.amount=v;}
    public String getCurrency(){return currency;} public void setCurrency(String v){this.currency=v;}
    public String getNote(){return note;} public void setNote(String v){this.note=v;}
    public Long getRecordedById(){return recordedById;} public void setRecordedById(Long v){this.recordedById=v;}
    public String getRecordedByName(){return recordedByName;} public void setRecordedByName(String v){this.recordedByName=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){this.createdAt=v;}
}


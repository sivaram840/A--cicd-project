package com.example.expensesplitter.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // foreign key group_id
    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    // created_by
    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // payer_id
    @ManyToOne(optional = false)
    @JoinColumn(name = "payer_id")
    private User payer;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(length = 10, nullable = false)
    private String currency = "INR";

    @Column(length = 20, nullable = false)
    private String splitType; // EQUAL, PERCENT, CUSTOM

    @Column(length = 1000)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ExpenseShare> shares = new HashSet<>();

    public Expense() {}

    // getters/setters
    public Long getId() { return id; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public User getPayer() { return payer; }
    public void setPayer(User payer) { this.payer = payer; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
    public Set<ExpenseShare> getShares() { return shares; }
    public void setShares(Set<ExpenseShare> shares) { this.shares = shares; }
    public void addShare(ExpenseShare share) {
        shares.add(share);
        share.setExpense(this);
    }
}


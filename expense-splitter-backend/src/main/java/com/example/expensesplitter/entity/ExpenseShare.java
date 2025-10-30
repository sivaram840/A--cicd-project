package com.example.expensesplitter.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_shares")
public class ExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // expense_id
    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    // user_id
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "share_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal shareAmount;

    @Column(name = "is_settled", nullable = false)
    private boolean isSettled = false;

    public ExpenseShare() {}

    // getters / setters
    public Long getId() { return id; }
    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BigDecimal getShareAmount() { return shareAmount; }
    public void setShareAmount(BigDecimal shareAmount) { this.shareAmount = shareAmount; }
    public boolean isSettled() { return isSettled; }
    public void setSettled(boolean settled) { isSettled = settled; }
}


package com.example.expensesplitter.dto;

import java.time.Instant;

public class MembershipDto {
    private Long id;
    private Long userId;
    private String name;   // changed from userName -> name
    private String role;
    private Instant joinedAt;

    public MembershipDto() {}

    public MembershipDto(Long id, Long userId, String name, String role, Instant joinedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }          // unified with frontend
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}

package com.example.expensesplitter.dto;

import java.time.Instant;
import java.util.List;

public class GroupDetailsDto {
    private Long id;
    private String name;
    private Long ownerId;
    private Instant createdAt;
    private List<MembershipDto> members;

    public GroupDetailsDto() {}

    public GroupDetailsDto(Long id, String name, Long ownerId, Instant createdAt, List<MembershipDto> members) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.members = members;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<MembershipDto> getMembers() { return members; }
    public void setMembers(List<MembershipDto> members) { this.members = members; }
}


package com.example.expensesplitter.repository;

import com.example.expensesplitter.entity.Membership;
import com.example.expensesplitter.entity.User;
import com.example.expensesplitter.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Membership entity.
 */
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    // Existing methods
    List<Membership> findByUser(User user);
    Optional<Membership> findByGroupAndUser(Group group, User user);

    // ✅ New method: fetch all memberships by group id
    List<Membership> findByGroup_Id(Long groupId);
}


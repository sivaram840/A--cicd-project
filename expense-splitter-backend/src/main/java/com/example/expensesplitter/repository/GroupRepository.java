package com.example.expensesplitter.repository;

import com.example.expensesplitter.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}


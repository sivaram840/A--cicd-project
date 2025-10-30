package com.example.expensesplitter.controller;

import com.example.expensesplitter.dto.UserProfile;
import com.example.expensesplitter.entity.User;
import com.example.expensesplitter.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/me")
    public UserProfile me(@CurrentUser User user) {
        // map entity -> DTO (avoid returning password hash)
        return new UserProfile(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}


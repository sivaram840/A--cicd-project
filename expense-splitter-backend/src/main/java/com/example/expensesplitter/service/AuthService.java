package com.example.expensesplitter.service;

import com.example.expensesplitter.dto.UserProfile;
import com.example.expensesplitter.entity.User;
import com.example.expensesplitter.repository.UserRepository;
import com.example.expensesplitter.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user. Throws IllegalArgumentException if email is already used.
     */
    @Transactional
    public User register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    /**
     * Authenticate user and return a JWT token if successful.
     * Throws IllegalArgumentException on invalid credentials.
     */
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Generate JWT token (subject=email) with basic claims (email, id, name)
        return jwtUtil.generateToken(user.getEmail(), user.getId(), user.getName());
    }

    /**
     * Get a safe profile for the currently logged-in user.
     * Used in AuthController.me() to return id, name, email, createdAt.
     */
    public UserProfile getProfile(User user) {
        if (user == null) return null;
        return new UserProfile(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}

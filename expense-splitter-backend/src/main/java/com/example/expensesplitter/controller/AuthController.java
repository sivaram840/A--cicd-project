package com.example.expensesplitter.controller;

import com.example.expensesplitter.dto.AuthResponse;
import com.example.expensesplitter.dto.LoginRequest;
import com.example.expensesplitter.dto.RegisterRequest;
import com.example.expensesplitter.dto.UserProfile;
import com.example.expensesplitter.entity.User;
import com.example.expensesplitter.security.CurrentUser;
import com.example.expensesplitter.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Constructor injection
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register endpoint: POST /api/auth/register
     * Body: { "name": "...", "email":"...", "password":"..." }
     * Returns: UserProfile of the newly registered user
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            User created = authService.register(req.getName(), req.getEmail(), req.getPassword());
            UserProfile profile = authService.getProfile(created);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error: " + e.getMessage());
        }
    }

    /**
     * Login endpoint: POST /api/auth/login
     * Body: { "email":"...", "password":"..." }
     * Returns: { "token": "...", "tokenType": "Bearer" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            String token = authService.login(req.getEmail(), req.getPassword());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error: " + e.getMessage());
        }
    }

    /**
     * Current user endpoint: GET /api/auth/me
     * Requires Authorization header: Bearer <token>
     * Returns: UserProfile of the logged-in user
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@CurrentUser User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        UserProfile profile = authService.getProfile(currentUser);
        return ResponseEntity.ok(profile);
    }
}

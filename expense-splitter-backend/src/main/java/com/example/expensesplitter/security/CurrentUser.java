package com.example.expensesplitter.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;

/**
 * @CurrentUser annotation:
 * Use this in controllers to get the current logged-in user/principal.
 * Example:
 *   public ResponseEntity<?> me(@CurrentUser User user) { ... }
 *
 * It is just a wrapper around Spring Security's @AuthenticationPrincipal.
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}


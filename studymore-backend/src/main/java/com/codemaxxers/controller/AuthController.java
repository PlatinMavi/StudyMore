package com.codemaxxers.controller;

import com.codemaxxers.model.User;
import com.codemaxxers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.Map;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            User user = userService.register(
                body.get("username"),
                body.get("email"),
                body.get("password")
            );
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        Optional<User> user = userService.login(
            body.get("email"),
            body.get("password")
        );
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }

    @PostMapping("/users/sync")
    public ResponseEntity<?> syncUser(@RequestBody Map<String, Object> body) {
        try {
            String username = (String) body.get("username");
            String email    = (String) body.get("email");
            Optional<User> existing = userService.findByEmail(email);
            if (existing.isPresent()) {
                return ResponseEntity.ok(Map.of("status", "exists"));
            }
            
            userService.register(username, email, "LOCAL_USER_NO_PASSWORD");
            return ResponseEntity.ok(Map.of("status", "created"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
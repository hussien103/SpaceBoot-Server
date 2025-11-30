package org.example.controller;

import org.example.entity.PlayerEntity;
import org.example.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Username and password are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean success = authenticationService.register(username, password);
        
        if (success) {
            response.put("success", true);
            response.put("message", "Registration successful");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Username already exists");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Username and password are required");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean success = authenticationService.login(username, password);
        
        if (success) {
            PlayerEntity player = authenticationService.getPlayer(username);
            if (player != null) {
                response.put("success", true);
                response.put("username", username);
                response.put("totalScore", player.getTotalScore());
                response.put("wins", player.getWins());
                response.put("gamesPlayed", player.getGamesPlayed());
                return ResponseEntity.ok(response);
            }
        }
        
        response.put("success", false);
        response.put("message", "Invalid credentials");
        return ResponseEntity.status(401).body(response);
    }
}


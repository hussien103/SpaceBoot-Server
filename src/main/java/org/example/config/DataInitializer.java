package org.example.config;

import org.example.entity.PlayerEntity;
import org.example.repository.PlayerRepository;
import org.example.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private LeaderboardService leaderboardService;
    
    @Override
    public void run(String... args) throws Exception {
        // Create default players if they don't exist
        boolean created = false;
        if (!playerRepository.existsByUsername("admin")) {
            PlayerEntity admin = new PlayerEntity("admin", "admin");
            playerRepository.save(admin);
            created = true;
            System.out.println("Created default admin user");
        }
        
        if (!playerRepository.existsByUsername("player1")) {
            PlayerEntity player1 = new PlayerEntity("player1", "pass1");
            playerRepository.save(player1);
            created = true;
            System.out.println("Created default player1 user");
        }
        
        if (created) {
            leaderboardService.broadcastLeaderboard();
        }
    }
}


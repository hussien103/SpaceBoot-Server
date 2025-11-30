package org.example.service;

import org.example.entity.PlayerEntity;
import org.example.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService {
    
    @Autowired
    private PlayerRepository playerRepository;
    
    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return false;
        }
        
        if (playerRepository.existsByUsername(username)) {
            return false; // Username already exists
        }
        
        PlayerEntity player = new PlayerEntity(username, password);
        playerRepository.save(player);
        return true;
    }
    
    public boolean login(String username, String password) {
        Optional<PlayerEntity> playerOpt = playerRepository.findByUsername(username);
        if (playerOpt.isPresent()) {
            PlayerEntity player = playerOpt.get();
            return player.getPassword().equals(password);
        }
        return false;
    }
    
    public PlayerEntity getPlayer(String username) {
        return playerRepository.findByUsername(username).orElse(null);
    }
    
    public List<PlayerEntity> getAllPlayers() {
        return playerRepository.findAll();
    }
    
    @Transactional
    public void updatePlayer(PlayerEntity player) {
        playerRepository.save(player);
    }
}


package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.PlayerEntity;
import org.example.model.LeaderboardEntry;
import org.example.repository.PlayerRepository;
import org.example.server.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private ConnectionManager connectionManager;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<LeaderboardEntry> getTopPlayers(int limit) {
        List<PlayerEntity> players = playerRepository.findAllOrderByTotalScoreDesc();
        
        return players.stream()
                .limit(limit)
                .map(player -> new LeaderboardEntry(
                    player.getUsername(),
                    player.getTotalScore(),
                    player.getWins(),
                    player.getGamesPlayed()
                ))
                .collect(Collectors.toList());
    }
    
    public List<LeaderboardEntry> getAllPlayers() {
        List<PlayerEntity> players = playerRepository.findAllOrderByTotalScoreDesc();
        System.out.println("Found " + players.size() + " players in database");
        
        List<LeaderboardEntry> entries = players.stream()
                .map(player -> new LeaderboardEntry(
                    player.getUsername(),
                    player.getTotalScore(),
                    player.getWins(),
                    player.getGamesPlayed()
                ))
                .collect(Collectors.toList());
        
        System.out.println("Created " + entries.size() + " leaderboard entries");
        return entries;
    }
    
    @Transactional
    public void updatePlayerStats(String username, int score, boolean won) {
        PlayerEntity player = authenticationService.getPlayer(username);
        if (player != null) {
            player.addGameResult(score, won);
            playerRepository.save(player);
            System.out.println("Updated stats for " + username + ": Score=" + player.getTotalScore() + 
                             ", Wins=" + player.getWins() + ", Games=" + player.getGamesPlayed());
            broadcastLeaderboard();
        }
    }
    
    public void broadcastLeaderboard() {
        List<LeaderboardEntry> entries = getAllPlayers();
        Map<String, Object> message = new HashMap<>();
        message.put("type", "LEADERBOARD");
        message.put("data", entries);
        connectionManager.broadcast(message, objectMapper);
    }
}


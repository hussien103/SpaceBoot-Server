package org.example.controller;

import org.example.model.LeaderboardEntry;
import org.example.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        List<LeaderboardEntry> leaderboard = leaderboardService.getAllPlayers();
        System.out.println("Leaderboard requested - returning " + leaderboard.size() + " entries");
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/top/{limit}")
    public ResponseEntity<List<LeaderboardEntry>> getTopPlayers(@PathVariable int limit) {
        List<LeaderboardEntry> leaderboard = leaderboardService.getTopPlayers(limit);
        return ResponseEntity.ok(leaderboard);
    }
}


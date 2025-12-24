package org.example.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Lobby;
import org.example.model.LobbyEntry;
import org.example.server.ConnectionManager;
import org.example.service.GameService;
import org.example.service.LobbiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lobbies")
@CrossOrigin(origins = "*")

public class LobbiesController {


    @Autowired
    private LobbiesService lobbiesService;
    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private GameService gameService;


    @GetMapping
    public List<Lobby> getLobbies() {

        return lobbiesService.getLobbies();


    }

    @PostMapping("/create")
    public Lobby createLobby(@RequestBody Lobby lobbyRequest, Principal principal) {
        // Always trust the authenticated user as host, ignore any spoofed host in request body
        String hostUsername = principal.getName();
        Lobby lobby = lobbiesService.createLobby(lobbyRequest.getName(), hostUsername);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOBBY_CREATED");
        message.put("data", lobby);
        connectionManager.broadcast(message, new ObjectMapper());

        return lobby;
    }

    @PostMapping("/{lobbyId}/start")
    public void startLobby(@PathVariable String lobbyId, Principal principal) {
        Lobby lobby = lobbiesService.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found: " + lobbyId));

        // Only the host is allowed to start the lobby
        if (!principal.getName().equals(lobby.getHost())) {
            throw new IllegalStateException("Only the host can start this lobby");
        }
        // All non-host players must be ready (host doesn't need to ready up)
        boolean allReady = lobby.getEntries().stream()
                .filter(entry -> !entry.getUsername().equals(lobby.getHost()))
                .allMatch(LobbyEntry::isReady);
        if (!allReady) {
            throw new IllegalStateException("All players must be ready before starting the game");
        }

        // Add all lobby players to the game (pass lobbyId so game can destroy lobby when finished)
        lobby.getEntries().forEach(entry -> {
            String sessionId = connectionManager.getSessionIdByUsername(entry.getUsername());
            if (sessionId != null) {
                gameService.addPlayerToGame(sessionId, entry.getUsername(), connectionManager.getChannel(sessionId), lobbyId);
            }
        });

        // Notify all lobby members that the game has started
        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOBBY_STARTED");
        Map<String, Object> data = new HashMap<>();
        data.put("lobbyId", lobbyId);
        message.put("data", data);
        connectionManager.broadcast(message, new ObjectMapper());
    }

    @PostMapping("/{lobbyId}/join")
    public Lobby joinLobby(@PathVariable String lobbyId, Principal principal) {
        String username = principal.getName();
        Lobby lobby = lobbiesService.joinLobby(lobbyId, username);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOBBY_UPDATED");
        message.put("data", lobby);
        connectionManager.broadcast(message, new ObjectMapper());

        return lobby;
    }

    @PostMapping("/{lobbyId}/message")
    public void sendLobbyMessage(@PathVariable String lobbyId,
                                 @RequestBody Map<String, String> body,
                                 Principal principal) {
        String username = principal.getName();
        String content = body.getOrDefault("message", "").trim();
        if (content.isEmpty()) {
            return;
        }

        Lobby lobby = lobbiesService.findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found: " + lobbyId));

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "LOBBY_MESSAGE");
        Map<String, Object> data = new HashMap<>();
        data.put("lobbyId", lobbyId);
        data.put("from", username);
        data.put("message", content);
        payload.put("data", data);

        ObjectMapper mapper = new ObjectMapper();
        // Send only to players in this lobby
        lobby.getEntries().forEach(entry -> {
            String sessionId = connectionManager.getSessionIdByUsername(entry.getUsername());
            if (sessionId != null) {
                connectionManager.sendToSession(sessionId, payload, mapper);
            }
        });
    }

    @PostMapping("/{lobbyId}/ready/toggle")
    public Lobby toggleReady(@PathVariable String lobbyId, Principal principal) {
        String username = principal.getName();
        Lobby lobby = lobbiesService.toggleReady(lobbyId, username);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOBBY_UPDATED");
        message.put("data", lobby);
        connectionManager.broadcast(message, new ObjectMapper());

        return lobby;
    }


}

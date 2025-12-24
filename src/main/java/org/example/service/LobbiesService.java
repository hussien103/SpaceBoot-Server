package org.example.service;


import org.example.entity.PlayerEntity;
import org.example.model.Lobby;
import org.example.model.LobbyEntry;
import org.example.repository.PlayerRepository;
import org.example.server.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LobbiesService {

    private final List<Lobby> lobbies;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ConnectionManager connectionManager;

    public LobbiesService() {
        lobbies = new ArrayList<>();
    }

    public synchronized Lobby createLobby(String name, String hostUsername) {
        String hostSessionId = connectionManager.getSessionIdByUsername(hostUsername);
        if (hostSessionId == null) {
            throw new IllegalStateException("Host is not connected via WebSocket: " + hostUsername);
        }

        Lobby lobby = new Lobby(name, hostUsername, hostSessionId);

        Optional<PlayerEntity> entityOpt = playerRepository.findByUsername(hostUsername);
        entityOpt.ifPresent(playerEntity -> lobby.getEntries().add(new LobbyEntry(playerEntity)));

        lobbies.add(lobby);
        return lobby;
    }

    public synchronized Lobby joinLobby(String lobbyId, String username) {
        Lobby lobby = findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found: " + lobbyId));

        // If already in lobby, just return current state
        boolean alreadyPresent = lobby.getEntries().stream()
                .anyMatch(entry -> username.equals(entry.getUsername()));
        if (alreadyPresent) {
            return lobby;
        }

        // Respect max players setting
        if (lobby.getEntries().size() >= lobby.getMaxPlayers()) {
            throw new IllegalStateException("Lobby is full");
        }

        PlayerEntity player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + username));

        lobby.getEntries().add(new LobbyEntry(player));
        return lobby;
    }

    public synchronized Lobby toggleReady(String lobbyId, String username) {
        Lobby lobby = findById(lobbyId)
                .orElseThrow(() -> new IllegalArgumentException("Lobby not found: " + lobbyId));

        lobby.getEntries().stream()
                .filter(entry -> username.equals(entry.getUsername()))
                .findFirst()
                .ifPresent(entry -> entry.setReady(!entry.isReady()));

        return lobby;
    }

    public synchronized Optional<Lobby> findById(String id) {
        return lobbies.stream().filter(l -> Objects.equals(l.getId(), id)).findFirst();
    }

    public synchronized void removeLobby(Lobby lobby) {
        lobbies.remove(lobby);
    }
    public List<Lobby> getLobbies() {
        return new ArrayList<>(lobbies);
    }

}

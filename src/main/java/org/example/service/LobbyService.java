package org.example.service;

import org.example.entity.PlayerEntity;
import org.example.model.LobbyEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LobbyService {

    private List<LobbyEntry> lobbyEntries = new ArrayList<>();

    public LobbyService() {
        this.lobbyEntries = new ArrayList<>();
    }
    public LobbyService(List<LobbyEntry> lobbyEntries) {
        this.lobbyEntries = lobbyEntries;
    }
    public LobbyEntry addLobbyEntry(PlayerEntity player) {


        LobbyEntry entry = new LobbyEntry(player);
        lobbyEntries.add(entry);
        return entry;
    }

}

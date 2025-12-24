package org.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Lobby {

    /**
     * Unique lobby identifier used by client/server to reference this lobby.
     */
    private String id;

    private List<LobbyEntry> entries = new ArrayList<>();
    private String name;
    private String host;
    private String hostSessionID;

    // Simple game settings that can be extended later
    private String map = "Desert";
    private String mode = "Deathmatch";
    private int maxPlayers = 4;

    public Lobby(String name, String host, String hostSessionID) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.host = host;
        this.hostSessionID = hostSessionID;
    }
    public Lobby(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<LobbyEntry> getEntries() {
        return entries;
    }
    public void setEntries(List<LobbyEntry> entries) {
        this.entries = entries;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getHostSessionID() {
        return hostSessionID;

    }
    public void setHostSessionID(String hostSessionID) {
        this.hostSessionID = hostSessionID;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }


}

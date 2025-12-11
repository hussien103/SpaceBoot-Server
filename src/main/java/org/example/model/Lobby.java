package org.example.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;


public class Lobby {

    private ArrayList<LobbyEntry> entries = new ArrayList<LobbyEntry>();
    private String name;
    private String host;
    private String hostSessionID;

    public Lobby(String name, String host, String hostSessionID) {
        this.name = name;
        this.host = host;
        this.hostSessionID = hostSessionID;
    }
    public Lobby(){

    }

    public ArrayList<LobbyEntry> getEntries() {
        return entries;
    }
    public void setEntries(ArrayList<LobbyEntry> entries) {
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


}

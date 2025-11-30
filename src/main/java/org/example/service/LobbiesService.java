package org.example.service;


import org.example.model.Lobby;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LobbiesService {

    private List<Lobby> lobbies;

    public LobbiesService() {
        lobbies = new ArrayList<Lobby>();
    }
    public void addLobby(Lobby lobby){
        lobbies.add(new Lobby());
    }
    public List<Lobby> getLobbies() {
        return lobbies;
    }
    public void setLobbies(List<Lobby> lobbies) {
        this.lobbies = lobbies;
    }

}

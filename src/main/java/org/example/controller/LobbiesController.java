package org.example.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Lobby;
import org.example.server.ConnectionManager;
import org.example.service.LobbiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping
    public List<Lobby> getLobbies() {

        return lobbiesService.getLobbies();


    }
    @PostMapping
    public void createLobby(@RequestBody Lobby lobby) {
        lobbiesService.addLobby(lobby);
        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOBBY_CREATED");
        message.put("data", lobby);
        connectionManager.broadcast(message,new ObjectMapper());
    }


}

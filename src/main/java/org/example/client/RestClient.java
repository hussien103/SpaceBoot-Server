package org.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.LeaderboardEntry;
import org.example.model.Lobby;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    
    public RestClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    public Map<String, Object> login(String username, String password) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("password", password);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Connection error: " + e.getMessage());
            return error;
        }
    }
    
    public Map<String, Object> register(String username, String password) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("password", password);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Connection error: " + e.getMessage());
            return error;
        }
    }
    
    public List<LeaderboardEntry> getLeaderboard() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/leaderboard"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<LeaderboardEntry> entries = objectMapper.readValue(response.body(),
                        new TypeReference<List<LeaderboardEntry>>() {});
                System.out.println("Retrieved " + entries.size() + " leaderboard entries");
                return entries;
            } else {
                System.err.println("Failed to get leaderboard. Status: " + response.statusCode() + ", Body: " + response.body());
                return List.of();
            }
        } catch (Exception e) {
            System.err.println("Error getting leaderboard: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    public List<Lobby> getLobbies() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/lobbies"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                return objectMapper.readValue(
                        response.body(),
                        new TypeReference<List<Lobby>>() {}
                );
            } else {
                System.err.println("Failed to get lobbies: " + response.statusCode());
                System.err.println(response.body());
                return List.of();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    public Map<String, Object> createLobby(String name, String host, String sessionId) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", name);
            requestBody.put("host", host);
            requestBody.put("hostSessionID", sessionId);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/lobbies/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<Map<String, Object>>() {}
            );

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Connection error: " + e.getMessage());
            return error;
        }
    }

}


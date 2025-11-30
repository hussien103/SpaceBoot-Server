# Spaceship Battle - Multiplayer Game

A high-performance Spring Boot multiplayer spaceship battle game with JavaFX client, using Netty for WebSocket communication.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## How to Run

### Step 1: Start the Spring Boot Server

Open a terminal and run:

```bash
mvn spring-boot:run
```

Or run the `SpaceshipBattleApplication` class directly from your IDE.

The server will start on:
- **REST API:** http://localhost:8080
- **WebSocket Server:** ws://localhost:8081/ws

You should see output like:
```
Netty WebSocket server started on port 8081
Started SpaceshipBattleApplication in X.XXX seconds
```

### Step 2: Start the JavaFX Client

Open a **new terminal** (keep the server running) and run:

```bash
mvn javafx:run
```

Or run the `Main` class directly from your IDE.

The JavaFX client window will open.

## Using the Application

1. **Login/Register:**
   - Use existing accounts: `admin/admin` or `player1/pass1`
   - Or register a new account

2. **View Leaderboard:**
   - Click "View Leaderboard" button on the login screen

3. **Play the Game:**
   - After logging in, you'll enter the game
   - **Controls:**
     - `W` or `↑` - Move forward
     - `S` or `↓` - Move backward
     - `A` or `←` - Strafe left
     - `D` or `→` - Strafe right
     - `Q` - Rotate left
     - `E` - Rotate right
     - `SPACE` - Shoot

4. **Multiplayer:**
   - Run multiple client instances to play with other players
   - Each player needs to login with a different account

## Project Structure

- **Backend (Spring Boot):**
  - `SpaceshipBattleApplication.java` - Main Spring Boot application
  - `controller/` - REST API endpoints
  - `service/` - Business logic (game, auth, leaderboard)
  - `model/` - Data models

- **Frontend (JavaFX):**
  - `Main.java` - JavaFX application entry point
  - `client/` - GUI screens (Login, Game, Leaderboard)

## API Endpoints

- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/leaderboard` - Get leaderboard

## WebSocket Communication

All game communication uses Netty WebSocket (ws://localhost:8081/ws):
- Real-time game state updates
- Player input handling
- Leaderboard updates
- Player join/leave notifications

## Architecture

- **Backend:** Spring Boot with Netty WebSocket server for high-performance real-time communication
- **Frontend:** JavaFX client with Netty WebSocket client
- **Database:** H2 (file-based) for player persistence

## Troubleshooting

- **Server won't start:** Make sure ports 8080 (REST) and 8081 (WebSocket) are not in use
- **Client can't connect:** Make sure the server is running first and WebSocket server started successfully
- **JavaFX errors:** Make sure you have JavaFX properly installed
- **WebSocket connection failed:** Check that Netty WebSocket server started on port 8081


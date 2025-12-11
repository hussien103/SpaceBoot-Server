# Spring Security Usage Guide for Your Multiplayer Game

## 📋 Current State Analysis

Your project has Spring Security dependency but is **NOT fully utilizing its features**. You have:
- ✅ Spring Security dependency in `pom.xml`
- ✅ Basic `SecurityConfig.java` with beans
- ✅ `Player` model implementing `UserDetails`
- ❌ **Custom authentication logic instead of Spring Security's built-in features**
- ❌ **No password encoding** (using `NoOpPasswordEncoder` - deprecated and insecure)
- ❌ **No HTTP security configuration** (endpoints are likely unprotected)
- ❌ **Manual login/register logic** instead of Spring Security's authentication

---

## 🔐 What is Spring Security?

Spring Security is a powerful authentication and authorization framework that provides:
1. **Authentication** - Verifying user identity (who you are)
2. **Authorization** - Checking user permissions (what you can do)
3. **Protection** - CSRF, session management, password encoding
4. **Integration** - Works seamlessly with Spring Boot

---

## 🏗️ How Spring Security Works

### Core Components:

```
┌─────────────────────────────────────────────────────────────┐
│                    HTTP Request                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              Security Filter Chain                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 1. Check if endpoint requires authentication         │  │
│  │ 2. Extract credentials (Basic Auth, JWT, Session)    │  │
│  │ 3. Call AuthenticationManager                        │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│           AuthenticationManager                              │
│  • Delegates to AuthenticationProvider(s)                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│        DaoAuthenticationProvider                             │
│  • Calls UserDetailsService to load user                    │
│  • Compares passwords using PasswordEncoder                 │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│            UserDetailsService                                │
│  • loadUserByUsername(String username)                      │
│  • Returns UserDetails object                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Your Current Implementation

### 1. **SecurityConfig.java** (Current)
```java
@Configuration
public class SecurityConfig {
    @Bean
    UserDetailsService userDetailsService() {
        UserDetails user = new Player("hussien","123");
        List<UserDetails> userDetailsList = new ArrayList<>();
        userDetailsList.add(user);
        return new InMemoryUserDetailsManager(userDetailsList);
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // ❌ INSECURE!
    }
}
```

**Problems:**
- ❌ No HTTP security configuration (all endpoints are open or uses default behavior)
- ❌ Using deprecated `NoOpPasswordEncoder` (passwords stored in plain text)
- ❌ Creating hardcoded user that's not used anywhere
- ❌ Not integrated with your database authentication

### 2. **AuthenticationService.java** (Current)
```java
public boolean login(String username, String password) {
    Optional<PlayerEntity> playerOpt = playerRepository.findByUsername(username);
    if (playerOpt.isPresent()) {
        PlayerEntity player = playerOpt.get();
        return player.getPassword().equals(password); // ❌ Plain text comparison!
    }
    return false;
}
```

**Problems:**
- ❌ Manual authentication bypassing Spring Security
- ❌ Plain text password comparison
- ❌ No session management
- ❌ No authentication context set in Spring Security

---

## ✅ Proper Spring Security Implementation

### Step 1: Update SecurityConfig.java

```java
package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    // 1. PASSWORD ENCODER - BCrypt for secure password hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. HTTP SECURITY - Define which endpoints require authentication
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for REST API
            .cors(cors -> cors.configure(http)) // Enable CORS
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers("/api/leaderboard/**").permitAll()
                .requestMatchers("/ws/**").permitAll() // WebSocket endpoint
                .requestMatchers("/h2-console/**").permitAll() // H2 database console
                
                // Protected endpoints (authentication required)
                .requestMatchers("/api/lobbies/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {}) // Enable HTTP Basic authentication
            .formLogin(form -> form.disable()) // Disable form login (we're using REST)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED
                )
            );
        
        // Allow H2 console frames
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        
        return http.build();
    }

    // 3. AUTHENTICATION PROVIDER - Connects UserDetailsService with PasswordEncoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 4. AUTHENTICATION MANAGER - Used for programmatic authentication
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
```

### Step 2: Create Proper UserDetailsService

```java
package org.example.service;

import org.example.entity.PlayerEntity;
import org.example.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PlayerEntity player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Convert PlayerEntity to Spring Security UserDetails
        return User.builder()
                .username(player.getUsername())
                .password(player.getPassword()) // Should be BCrypt encoded
                .roles("USER") // You can add roles/authorities
                .build();
    }
}
```

### Step 3: Update AuthenticationService

```java
package org.example.service;

import org.example.entity.PlayerEntity;
import org.example.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return false;
        }
        
        if (playerRepository.existsByUsername(username)) {
            return false;
        }
        
        // Encode password using BCrypt
        String encodedPassword = passwordEncoder.encode(password);
        PlayerEntity player = new PlayerEntity(username, encodedPassword);
        playerRepository.save(player);
        return true;
    }
    
    public boolean login(String username, String password) {
        try {
            // Use Spring Security's AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public PlayerEntity getPlayer(String username) {
        return playerRepository.findByUsername(username).orElse(null);
    }
    
    public PlayerEntity getCurrentPlayer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return getPlayer(username);
        }
        return null;
    }
}
```

### Step 4: Update PlayerEntity

Make sure your `PlayerEntity` stores BCrypt encoded passwords:

```java
package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class PlayerEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password; // BCrypt encoded
    
    private int totalScore = 0;
    private int gamesPlayed = 0;
    private int wins = 0;
    
    // Constructors, getters, setters...
}
```

---

## 🎯 How It Works Together

### Registration Flow:
```
1. User sends POST /api/auth/register with {username, password}
2. AuthController receives request
3. AuthenticationService.register() is called
4. Password is encoded using BCryptPasswordEncoder
5. PlayerEntity saved to database with encoded password
6. Success response sent to user
```

### Login Flow:
```
1. User sends POST /api/auth/login with {username, password}
2. AuthController receives request
3. AuthenticationService.login() is called
4. AuthenticationManager.authenticate() is invoked
5. DaoAuthenticationProvider gets user via CustomUserDetailsService
6. Password is compared using BCryptPasswordEncoder
7. If valid, Authentication object is created
8. SecurityContext is updated with authentication
9. User is now authenticated for subsequent requests
```

### Protected Endpoint Access:
```
1. User sends request to /api/lobbies/create
2. Security Filter Chain intercepts request
3. Checks if endpoint requires authentication (YES)
4. Checks SecurityContext for valid authentication
5. If authenticated, request proceeds to controller
6. If not authenticated, returns 401 Unauthorized
```

---

## 🔒 Security Best Practices

### 1. **Password Encoding**
```java
// ❌ NEVER DO THIS
String password = "mypassword";
player.setPassword(password); // Plain text!

// ✅ ALWAYS DO THIS
String encodedPassword = passwordEncoder.encode(password);
player.setPassword(encodedPassword); // BCrypt hashed
```

### 2. **Get Current User**
```java
// In any controller or service
@GetMapping("/profile")
public ResponseEntity<PlayerEntity> getProfile() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    PlayerEntity player = playerRepository.findByUsername(username).orElseThrow();
    return ResponseEntity.ok(player);
}

// Or inject Principal
@GetMapping("/profile")
public ResponseEntity<PlayerEntity> getProfile(Principal principal) {
    String username = principal.getName();
    // ...
}

// Or use @AuthenticationPrincipal
@GetMapping("/profile")
public ResponseEntity<PlayerEntity> getProfile(
        @AuthenticationPrincipal UserDetails userDetails) {
    String username = userDetails.getUsername();
    // ...
}
```

### 3. **Role-Based Authorization**
```java
// In SecurityConfig
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

// In Controller
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/players")
public List<PlayerEntity> getAllPlayers() {
    return playerRepository.findAll();
}
```

---

## 🚀 Testing Your Security

### 1. **Test Registration**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'
```

### 2. **Test Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'
```

### 3. **Test Protected Endpoint**
```bash
# Without authentication - should return 401
curl http://localhost:8080/api/lobbies/list

# With HTTP Basic authentication
curl -u testuser:testpass123 http://localhost:8080/api/lobbies/list
```

---

## 📚 Key Concepts Summary

| Component | Purpose | Your Implementation |
|-----------|---------|---------------------|
| **SecurityFilterChain** | Define which endpoints are protected | ❌ Missing - needs to be added |
| **UserDetailsService** | Load user from database | ⚠️ Exists but not properly used |
| **PasswordEncoder** | Hash/compare passwords securely | ❌ Using insecure NoOpPasswordEncoder |
| **AuthenticationManager** | Authenticate users | ❌ Not used - doing manual auth |
| **SecurityContext** | Store current user's authentication | ❌ Not being set |

---

## 🎓 Next Steps

1. ✅ **Update SecurityConfig** with SecurityFilterChain
2. ✅ **Change to BCryptPasswordEncoder** 
3. ✅ **Create CustomUserDetailsService**
4. ✅ **Update AuthenticationService** to use Spring Security
5. ✅ **Update existing passwords** in database (they need to be re-encoded)
6. ✅ **Test registration and login**
7. ✅ **Secure your WebSocket connections** (advanced)

---

## 💡 Additional Resources

- [Spring Security Official Docs](https://spring.io/projects/spring-security)
- [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture)
- [BCrypt Password Encoder](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/crypto/bcrypt/BCryptPasswordEncoder.html)

---

**Remember:** Spring Security is already in your project, but you're not using it properly! The examples above show how to integrate it correctly for better security and standard Spring practices.

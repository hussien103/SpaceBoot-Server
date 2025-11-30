package org.example.config;


import org.example.model.Player;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
        return NoOpPasswordEncoder.getInstance();

    }
}

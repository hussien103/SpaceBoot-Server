package org.example.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.List;

public class InMemoryUserDetailsManager implements UserDetailsService {

    private final List<UserDetails> userDetailsList;

    public InMemoryUserDetailsManager(List<UserDetails> userDetailsList) {
        this.userDetailsList = userDetailsList;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        for (UserDetails user : userDetailsList) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}

package com.lssgoo.mail.security;

import com.lssgoo.mail.entity.User;
import com.lssgoo.mail.repository.UserRepository;
import com.lssgoo.mail.utils.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerUtil.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("Loading user by username/email: {}", usernameOrEmail);
        User user = userRepository.findActiveByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", usernameOrEmail);
                    return new UsernameNotFoundException("User not found: " + usernameOrEmail);
                });

        logger.debug("User loaded successfully: {} (ID: {})", user.getUsername(), user.getId());
        return buildUserDetails(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        logger.debug("Loading user by ID: {}", id);
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with id: {}", id);
                    return new UsernameNotFoundException("User not found with id: " + id);
                });

        logger.debug("User loaded successfully by ID: {} (Username: {})", id, user.getUsername());
        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        // Add default role if needed
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
}


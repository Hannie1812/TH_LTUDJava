package com.nbhang.services;

import com.nbhang.constants.Provider;
import com.nbhang.constants.Role;
import com.nbhang.entities.User;
import com.nbhang.repositories.IRoleRepository;
import com.nbhang.repositories.IUserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IRoleRepository roleRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
    public void save(@NotNull User user) {
        user.setPassword(new BCryptPasswordEncoder()
                .encode(user.getPassword()));
        userRepository.save(user);
    }

    public java.util.Optional<User> findByUsername(@NotNull String username) {
        return java.util.Optional.ofNullable(userRepository.findByUsername(username));
    }

    public void saveOauthUser(String email, @NotNull String username) {
        log.info("Attempting to save OAuth user with email: {}", email);
        if (userRepository.findByEmail(email) != null) {
            log.info("User with email {} already exists", email);
            return;
        }
        try {
            var user = new User();
            user.setUsername(email);
            user.setEmail(email);
            user.setPassword(new BCryptPasswordEncoder().encode(email));
            user.setProvider(Provider.GOOGLE.value);
            user.getRoles().add(roleRepository.findRoleById(Role.USER.value));
            userRepository.save(user);
            log.info("Successfully saved OAuth user with email: {}", email);
        } catch (Exception e) {
            log.error("Error saving OAuth user with email: {}", email, e);
            throw e;
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
    public void setDefaultRole(String username) {
        userRepository.findByUsername(username)
                .getRoles()
                .add(roleRepository
                        .findRoleById(Role.USER.value));
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        var user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getAuthorities())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
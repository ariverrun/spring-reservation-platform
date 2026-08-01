package com.example.auth.service;

import com.example.auth.dto.RegisterRequestDto;
import com.example.auth.dto.UserResponseDto;
import com.example.auth.entity.Role;
import com.example.auth.entity.User;
import com.example.auth.event.UserRegisteredEvent;
import com.example.auth.exceptions.EmailAlreadyUsedException;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        validateEmailNotExists(request.email());
        
        Role userRole = getDefaultRole();
        User user = buildUser(request, userRole);
        User savedUser = userRepository.save(user);
        
        publishUserRegisteredEvent(savedUser);
        log.info("User registered with id: {}", savedUser.getId());
        
        return new UserResponseDto(savedUser.getId());
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException("Email already registered");
        }
    }

    private Role getDefaultRole() {
        return roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
    }

    private User buildUser(RegisterRequestDto request, Role role) {
        return User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(role))
                .build();
    }

    private void publishUserRegisteredEvent(User user) {
        eventPublisher.publishEvent(
            new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
            )
        );
    }
}
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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException("Email already registered");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        User user = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);

        eventPublisher.publishEvent(
            new UserRegisteredEvent(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName()
            )
        );

        log.info("User registered with id: {}", savedUser.getId());

        return new UserResponseDto(savedUser.getId());
    }
}
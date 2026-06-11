package com.nationlens.service;

import com.nationlens.domain.entity.Role;
import com.nationlens.domain.entity.User;
import com.nationlens.dto.admin.UserListDto;
import com.nationlens.repository.RoleRepository;
import com.nationlens.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public Page<UserListDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toDto);
    }

    public Optional<UserListDto> findById(Long id) {
        return userRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public UserListDto updateRoles(Long userId, List<String> roleCodes) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Set<Role> newRoles = new HashSet<>();
        for (String code : roleCodes) {
            Role role = roleRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + code));
            newRoles.add(role);
        }

        user.setRoles(newRoles);
        userRepository.save(user);
        return toDto(user);
    }

    @Transactional
    public UserListDto setActive(Long userId, Boolean active) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setIsActive(active);
        userRepository.save(user);
        return toDto(user);
    }

    private UserListDto toDto(User user) {
        return UserListDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .mobile(user.getMobile())
            .displayName(user.getDisplayName())
            .isActive(user.getIsActive())
            .emailVerified(user.getEmailVerified())
            .gamificationLevel(user.getGamificationLevel())
            .contributionScore(user.getContributionScore())
            .createdAt(user.getCreatedAt())
            .roles(user.getRoles().stream().map(Role::getCode).toList())
            .build();
    }
}

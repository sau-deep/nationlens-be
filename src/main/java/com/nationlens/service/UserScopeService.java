package com.nationlens.service;

import com.nationlens.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserScopeService {

    private final JdbcTemplate jdbcTemplate;

    public record UserScope(String primaryRole, Long stateId, Long districtId, String stateCode) {}

    public UserScope resolve(User user) {
        if (user == null) return new UserScope("CITIZEN", null, null, null);

        String primaryRole = user.getRoles().stream()
            .map(r -> r.getCode())
            .filter(code -> !"CITIZEN".equals(code))
            .findFirst()
            .orElse("CITIZEN");

        Long stateId = user.getStateId();
        Long districtId = user.getDistrictId();
        String stateCode = null;

        // Fallback to user_roles scope columns when user fields are unset
        if (stateId == null || districtId == null) {
            Optional<ScopeRow> scope = loadScopeFromDb(user.getId(), primaryRole);
            if (scope.isPresent()) {
                if ("STATE".equals(scope.get().scopeType()) && stateId == null) {
                    stateId = scope.get().scopeId();
                }
                if ("DISTRICT".equals(scope.get().scopeType()) && districtId == null) {
                    districtId = scope.get().scopeId();
                }
            }
        }

        if (stateId != null) {
            stateCode = jdbcTemplate.query(
                "SELECT code FROM states WHERE id = ?",
                rs -> rs.next() ? rs.getString(1) : null,
                stateId
            );
        } else if (districtId != null) {
            stateCode = jdbcTemplate.query(
                "SELECT s.code FROM districts d JOIN states s ON s.id = d.state_id WHERE d.id = ?",
                rs -> rs.next() ? rs.getString(1) : null,
                districtId
            );
            if (stateId == null) {
                stateId = jdbcTemplate.query(
                    "SELECT state_id FROM districts WHERE id = ?",
                    rs -> rs.next() ? rs.getLong(1) : null,
                    districtId
                );
            }
        }

        return new UserScope(primaryRole, stateId, districtId, stateCode);
    }

    public boolean hasRole(User user, String role) {
        return user.getRoles().stream().anyMatch(r -> role.equals(r.getCode()));
    }

    public boolean hasAnyRole(User user, Set<String> roles) {
        return user.getRoles().stream().anyMatch(r -> roles.contains(r.getCode()));
    }

    private Optional<ScopeRow> loadScopeFromDb(Long userId, String roleCode) {
        var rows = jdbcTemplate.query(
            """
            SELECT ur.scope_type, ur.scope_id
            FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = ? AND r.code = ?
            LIMIT 1
            """,
            (rs, rowNum) -> new ScopeRow(rs.getString(1), rs.getObject(2) != null ? rs.getLong(2) : null),
            userId, roleCode
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    private record ScopeRow(String scopeType, Long scopeId) {}
}

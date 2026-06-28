package com.nationlens.dto.stateadmin;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StateUserItemDto {
    private Long id;
    private String displayName;
    private String email;
    private List<String> roles;
    private Boolean isActive;
}

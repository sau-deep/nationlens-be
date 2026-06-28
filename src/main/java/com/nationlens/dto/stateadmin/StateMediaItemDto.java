package com.nationlens.dto.stateadmin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StateMediaItemDto {
    private Long id;
    private String titleEn;
    private String platform;
    private String approvalStatus;
    private String sourceOwner;
    private LocalDateTime createdAt;
}

package com.nationlens.dto.poll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PollDto {
    private Long id;
    private String questionEn;
    private String questionHi;
    private Long entityId;
    private Long districtId;
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private List<PollOptionDto> options;
    private Long totalVotes;
}

package com.nationlens.dto.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class CommentDto {
    private Long id;
    private String body;
    private Long parentId;
    private Long userId;
    private String userDisplayName;
    private LocalDateTime createdAt;
}

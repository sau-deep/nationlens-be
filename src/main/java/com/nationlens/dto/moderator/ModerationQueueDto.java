package com.nationlens.dto.moderator;

import com.nationlens.dto.media.MediaLinkDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationQueueDto {
    private List<MediaLinkDto> pendingMedia;
    private List<CommentQueueItem> pendingComments;
    private int totalPending;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentQueueItem {
        private Long id;
        private String body;
        private String userDisplayName;
        private Long mediaLinkId;
        private String moderationStatus;
        private LocalDateTime createdAt;
    }
}

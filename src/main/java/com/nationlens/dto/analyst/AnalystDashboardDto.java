package com.nationlens.dto.analyst;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalystDashboardDto {
    private long totalMedia;
    private long approvedMedia;
    private long pendingMedia;
    private long rejectedMedia;
    private long totalEntities;
    private long totalComments;
    private long activePolls;
    private long totalUsers;
    private long rssNewsItems;
    private long pendingComments;
}

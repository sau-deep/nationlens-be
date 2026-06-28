package com.nationlens.dto.stateadmin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StateAdminDashboardDto {
    private String stateName;
    private String stateCode;
    private long entityCount;
    private long mediaCount;
    private long pendingMediaCount;
    private long districtCount;
}

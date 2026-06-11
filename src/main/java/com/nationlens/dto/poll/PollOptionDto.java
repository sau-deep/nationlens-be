package com.nationlens.dto.poll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PollOptionDto {
    private Long id;
    private String labelEn;
    private String labelHi;
    private Integer displayOrder;
    private Long voteCount;
    private Double votePercentage;
}

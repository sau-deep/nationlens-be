package com.nationlens.dto.poll;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VoteRequest {
    @NotNull
    private Long pollOptionId;
}

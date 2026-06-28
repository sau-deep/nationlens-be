package com.nationlens.dto.poll;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Create/update payload for admin-managed polls.
 */
@Getter @Setter
public class AdminPollRequest {

    @NotBlank
    @Size(max = 500)
    private String questionEn;

    @Size(max = 500)
    private String questionHi;

    private Long entityId;

    private Long districtId;

    private Boolean isActive;

    private LocalDateTime expiresAt;

    @NotEmpty
    @Valid
    private List<Option> options;

    @Getter @Setter
    public static class Option {
        /** Present on update for existing options; null for newly added options. */
        private Long id;

        @NotBlank
        @Size(max = 255)
        private String labelEn;

        @Size(max = 255)
        private String labelHi;

        private Integer displayOrder;
    }
}

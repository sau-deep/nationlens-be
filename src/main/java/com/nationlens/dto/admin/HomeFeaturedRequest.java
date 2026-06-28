package com.nationlens.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HomeFeaturedRequest {
    private Boolean isFeatured;
    private Integer homeDisplayOrder;
}

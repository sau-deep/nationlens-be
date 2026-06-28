package com.nationlens.controller.pub;

import com.nationlens.dto.common.ApiResponse;
import com.nationlens.dto.insight.AiInsightDto;
import com.nationlens.service.AiInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/insights")
@RequiredArgsConstructor
public class PublicAiInsightController {

    private final AiInsightService aiInsightService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiInsightDto>>> list(
        @RequestParam(defaultValue = "10") int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return ResponseEntity.ok(ApiResponse.ok(aiInsightService.listPublishedForHome(safeLimit)));
    }
}

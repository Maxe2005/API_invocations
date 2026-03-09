package com.imt.api_invocations.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.imt.api_invocations.service.StatsService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/invocation/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/verify-ranks-drop-rate")
    public ResponseEntity<String> verifyRanksDropRate() {
        boolean isValid = statsService.verifyCorrectRanksDropRate();
        if (isValid) {
            return ResponseEntity.ok("Ranks drop rates are valid and sum up to 100%.");
        }
        return ResponseEntity
                .ok("Ranks drop rates are NOT valid and do not sum up to 100%.\nPlease check the configuration.");
    }

    @GetMapping("get-loot-rate")
    public ResponseEntity<String> getLootRatesString(
            @Parameter(description = "Type de taux à retourner", schema = @Schema(type = "string", allowableValues = {
                    "all", "Theoretical Drop Rates",
                    "Real Drop Rates" }, defaultValue = "all")) @RequestParam(value = "type", defaultValue = "all") String type) {
        String result;
        switch (type.toLowerCase()) {
            case "theoretical drop rates":
                result = statsService.getTheoreticalLootRatesString();
                break;
            case "real drop rates":
                result = statsService.getRealLootRatesString();
                break;
            case "all":
            default:
                result = statsService.getLootRatesString();
                break;
        }
        return ResponseEntity.ok(result);
    }

}

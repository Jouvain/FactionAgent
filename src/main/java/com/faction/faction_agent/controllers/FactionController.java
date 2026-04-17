package com.faction.faction_agent.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.faction.faction_agent.models.FactionDraft;
import com.faction.faction_agent.services.FactionGenerationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/faction")
@Tag(name = "Faction", description = "Endpoints for faction generation using LLM")
@CrossOrigin(origins = "http://localhost:4200")
public class FactionController {

    private final FactionGenerationService factionGenerationService;

    public FactionController(FactionGenerationService factionGenerationService) {
        this.factionGenerationService = factionGenerationService;
    }

    @GetMapping("/draft")
    @Operation(summary = "Generate a draft faction", description = "Generates a preliminary faction draft from the LLM before full validation or correction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Draft faction successfully generated"),
            @ApiResponse(responseCode = "500", description = "Error during generation")
    })
    public FactionDraft generateFactionDraft() {
        return factionGenerationService.geneFactionDraft();
    }

    @GetMapping("/raw")
    @Operation(summary = "Generate raw LLM output", description = "Returns the raw unprocessed response from the LLM. Useful for debugging prompt behavior and parsing issues.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Raw response returned"),
            @ApiResponse(responseCode = "500", description = "Error during LLM call")
    })
    public String generateFactionRaw() {
        return factionGenerationService.generateFactionRaw();
    }

    @GetMapping("/generate")
    @Operation(summary = "Generate a validated faction", description = "Generates a faction using the LLM with validation, JSON parsing, and retry logic to ensure a clean structured output")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Faction successfully generated"),
            @ApiResponse(responseCode = "500", description = "Generation or parsing failed")
    })
    public FactionDraft generateFaction() {
        return factionGenerationService.generateFaction();
    }

    @GetMapping("/random")
    @Operation(summary = "Generate a random-driven faction", description = "Generates a faction using randomized prompt variations or parameters to increase diversity of outputs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Random faction successfully generated"),
            @ApiResponse(responseCode = "500", description = "Error during generation")
    })
    public FactionDraft generateRandomLedFaction() {
        return factionGenerationService.generateRandomLedFaction();
    }

}

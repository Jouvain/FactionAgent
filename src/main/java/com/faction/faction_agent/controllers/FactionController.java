package com.faction.faction_agent.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.faction.faction_agent.models.FactionDraft;
import com.faction.faction_agent.services.FactionGenerationService;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class FactionController {

    private final FactionGenerationService factionGenerationService;

    public FactionController(FactionGenerationService factionGenerationService) {
        this.factionGenerationService = factionGenerationService;
    }

    @GetMapping("/faction")
    public FactionDraft generateFaction() {
        return factionGenerationService.geneFactionDraft();
    }
    

}

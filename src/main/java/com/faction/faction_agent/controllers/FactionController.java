package com.faction.faction_agent.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.faction.faction_agent.models.FactionDraft;
import com.faction.faction_agent.services.FactionGenerationService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;





@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class FactionController {

    private final FactionGenerationService factionGenerationService;

    public FactionController(FactionGenerationService factionGenerationService) {
        this.factionGenerationService = factionGenerationService;
    }

    @GetMapping("/faction/draft")
    public FactionDraft generateFactionDraft() {
        return factionGenerationService.geneFactionDraft();
    }

    @GetMapping("faction/raw")
    public String generateFactionRaw() {
        return factionGenerationService.generateFactionRaw();
    }
    
    @GetMapping("faction/generate")
    public FactionDraft generateFaction() {
        return factionGenerationService.generateFaction();
    }

    @GetMapping("faction/random")
    public FactionDraft generateRandomLedFaction() {
        return factionGenerationService.generateRandomLedFaction();
    }
    
    

}

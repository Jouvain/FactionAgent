package com.faction.faction_agent.services;

import org.springframework.stereotype.Service;

import com.faction.faction_agent.models.FactionDraft;

@Service
public class FactionGenerationService {
    public FactionDraft geneFactionDraft() {
        return new FactionDraft(
            "Les veilleurs de'Obsidienne",
            "Secte",
            "Contrôler les artefacts interdits de l'Obsidienne"
        );
    }
}

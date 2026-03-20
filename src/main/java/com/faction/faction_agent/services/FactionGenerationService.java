package com.faction.faction_agent.services;

import org.springframework.stereotype.Service;

import com.faction.faction_agent.llm.OllamaClient;
import com.faction.faction_agent.models.FactionDraft;

import tools.jackson.databind.ObjectMapper;

@Service
public class FactionGenerationService {

    private final OllamaClient ollamaClient;

    public FactionGenerationService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public String generateFactionRaw() {
        String prompt = """
                You are a strict JSON generator.

                You MUST return ONLY a valid JSON object.
                No explanation.
                No markdown.
                No text before or after.
                No comments.

                Rules:
                - Output must be valid JSON
                - Use double quotes for all strings
                - Do not use trailing commas
                - Do not add extra fields
                - Respect the exact field names
                - All text values must be short (max 15 words)

                Language: French

                JSON format:

                {
                  "name": "string",
                  "typeFaction": "string",
                  "objectif": "string"
                }

                Generate a faction.
                                                """;
        return ollamaClient.generate(prompt);
    }

    public FactionDraft geneFactionDraft() {
        return new FactionDraft(
                "Les veilleurs de'Obsidienne",
                "Secte",
                "Contrôler les artefacts interdits de l'Obsidienne");
    }

    public FactionDraft generateFaction() {
        int maxAttempts = 3;
        int attempts = 0;
        while (attempts < maxAttempts) {
            attempts++;
            String json = generateFactionRaw();
            try {
                ObjectMapper mapper = new ObjectMapper();
                FactionDraft faction = mapper.readValue(json, FactionDraft.class);

                if(isValid(faction)) {
                    return faction;
                }
            } catch (Exception e) {
                System.out.println("Parsing failed (attempt " + attempts + ")");
            }
        }
        throw new RuntimeException("LLM failed after " + maxAttempts + " attempts");
    }

    private boolean isValid(FactionDraft faction) {
        return faction.getName() != null && !faction.getName().isBlank()
            && faction.getObjectif() != null && !faction.getObjectif().isBlank()
            && faction.getTypeFaction() != null && !faction.getTypeFaction().isBlank();
    }


}

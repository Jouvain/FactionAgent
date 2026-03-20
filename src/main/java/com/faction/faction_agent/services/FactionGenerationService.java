package com.faction.faction_agent.services;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.faction.faction_agent.enums.TypeFaction;
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

                IMPORTANT :
                - typeFaction MUST be one of the following EXACT values :
                SECTE_CULTE, CABALE_GOUV, MAFIA_GUILDE, HORDE_LEGION, CLAN_DYNASTIE, BANDE_COMPAGNIE
                - the name and desc MUST be consistent with the typeFaction.

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
                  "objectif": "string",
                  "desc": "string"
                }

                Generate a coherent and original faction for a role-playing game.
                """;
        return ollamaClient.generate(prompt);
    }

    public FactionDraft geneFactionDraft() {
        return new FactionDraft(
                "Les veilleurs de'Obsidienne",
                "Secte",
                "Contrôler les artefacts interdits de l'Obsidienne",
                "Une poignée de vieilles familles de la région, qui utilisent leur argent et entregent pour accomplir leur devoir.");
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

                if (isValid(faction)) {
                    return faction;
                }
            } catch (Exception e) {
                System.out.println("Parsing failed (attempt " + attempts + ")");
            }
        }
        throw new RuntimeException("LLM failed after " + maxAttempts + " attempts");
    }

    private TypeFaction randomizeType() {
        TypeFaction[] values = TypeFaction.values();
        return values[new Random().nextInt(values.length)];
    }

    public FactionDraft generateRandomLedFaction() {
        int maxAttempts = 3;
        int attempts = 0;
        while (attempts < maxAttempts) {
            attempts++;
            String json = generateLedFactionRaw(randomizeType());
            try {
                ObjectMapper mapper = new ObjectMapper();
                FactionDraft faction = mapper.readValue(json, FactionDraft.class);

                if (isValid(faction)) {
                    return faction;
                }
            } catch (Exception e) {
                System.out.println("Parsing failed (attempt " + attempts + ")");
            }
        }
        throw new RuntimeException("LLM failed after " + maxAttempts + " attempts");
    }

    public String generateLedFactionRaw(TypeFaction type) {
        String prompt = """
                You are a strict JSON generator.

                You MUST return ONLY a valid JSON object.
                No explanation.
                No markdown.
                No text before or after.
                No comments.

                IMPORTANT :
                - typeFaction MUST be exactly: %s
                - the name and desc MUST be consistent with the typeFaction.

                Guidelines:
                - SECTE_CULTE → mystical, religious, secret rituals
                - CABALE_GOUV → political, hidden power, influence
                - MAFIA_GUILDE → economic, criminal or professional network
                - HORDE_LEGION → military, war, conquest
                - CLAN_DYNASTIE → family, heritage, lineage
                - BANDE_COMPAGNIE → small group, mercenaries, informal

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
                  "objectif": "string",
                  "desc": "string"
                }

                Generate a coherent and original faction for a role-playing game.
                """.formatted(type.name());
        return ollamaClient.generate(prompt);
    }

    private boolean isValid(FactionDraft faction) {
        return faction.getName() != null && !faction.getName().isBlank()
                && faction.getObjectif() != null && !faction.getObjectif().isBlank()
                && faction.getTypeFaction() != null && !faction.getTypeFaction().isBlank()
                && faction.getDesc() != null && !faction.getDesc().isBlank() && faction.getDesc().length() <= 300
                && isValidTypeFaction(faction.getTypeFaction());
    }

    private boolean isValidTypeFaction(String type) {
        try {
            TypeFaction.valueOf(type);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

package com.faction.faction_agent.services;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.faction.faction_agent.enums.FactionContext;
import com.faction.faction_agent.enums.TypeFaction;
import com.faction.faction_agent.llm.OllamaClient;
import com.faction.faction_agent.models.AgentState;
import com.faction.faction_agent.models.FactionDraft;
import com.faction.faction_agent.validation.ValidationResult;

import tools.jackson.databind.ObjectMapper;

@Service
public class FactionGenerationService {

    private final OllamaClient ollamaClient;

    public FactionGenerationService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    // #region public Methods

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

    public FactionDraft generateRandomLedFaction() {
        int maxAttempts = 3;

        AgentState agentState = new AgentState();

        while (agentState.getAttempts() < maxAttempts) {
            agentState.incrementAttempts();
            String json = generateLedFactionRaw(randomizeType(), randomizeContext());
            agentState.setLastJson(json);
            try {
                ObjectMapper mapper = new ObjectMapper();
                FactionDraft faction = mapper.readValue(json, FactionDraft.class);
                ValidationResult validation = validate(faction);
                if (validation.isValid()) {
                    return faction;
                }

                agentState.setLastErrors(validation.getErrors());
                String nextPrompt = decideNextPrompt(agentState, json);
                json = ollamaClient.generate(nextPrompt);
            } catch (Exception e) {
                System.out.println("Parsing failed (attempt " + agentState.getAttempts() + ")");
                List<String> errors = List.of("Invalid JSON format");
                agentState.setLastErrors(errors);

                String nextPrompt = buildCorrectionPrompt(json, errors);
                json = ollamaClient.generate(nextPrompt);
            }
        }
        throw new RuntimeException("LLM failed after " + maxAttempts + " attempts");
    }

    public String generateLedFactionRaw(TypeFaction type, FactionContext context) {
        String prompt = """
                You are a strict JSON generator.

                You MUST return ONLY a valid JSON object.
                No explanation.
                No markdown.
                No text before or after.
                No comments.

                IMPORTANT :
                - typeFaction MUST be exactly: %s
                - The name, objectif and desc must be coherent with BOTH type and context
                Context :
                - name = %s
                - guidelines = %s

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
                """.formatted(
                type.name(),
                context.name(),
                getContextDescription(context));
        return ollamaClient.generate(prompt);
    }

    // #endregion

    // #region private methods

    private boolean isValidTypeFaction(String type) {
        try {
            TypeFaction.valueOf(type);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValid(FactionDraft faction) {
        return faction.getName() != null && !faction.getName().isBlank()
                && faction.getObjectif() != null && !faction.getObjectif().isBlank()
                && faction.getTypeFaction() != null && !faction.getTypeFaction().isBlank()
                && faction.getDesc() != null && !faction.getDesc().isBlank() && faction.getDesc().length() <= 300
                && isValidTypeFaction(faction.getTypeFaction());
    }

    private ValidationResult validate(FactionDraft faction) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        if (faction.getName() == null || faction.getName().isBlank()) {
            result.addError("Name is missing or empty");
        }
        if (faction.getObjectif() == null || faction.getObjectif().isBlank()) {
            result.addError("Objectif is missing or empty");
        }
        if (faction.getTypeFaction() == null || faction.getTypeFaction().isBlank()) {
            result.addError("TypeFaction is missing or empty");
        } else if (!isValidTypeFaction(faction.getTypeFaction())) {
            result.addError("TypeFaction is invalid. Must be one of enum values");
        }
        if (faction.getDesc() == null || faction.getDesc().isBlank()) {
            result.addError("Desc is missing or empty");
        } else if (faction.getDesc().length() > 300) {
            result.addError("Desc is too long (max 300 characters)");
        }

        return result;
    }

    private String getContextDescription(FactionContext context) {
        return switch (context) {
            case MEDIEVAL_REALISTE -> """
                        Realistic medieval setting.
                        No magic or supernatural elements.
                        Factions are political, religious or military.
                        Names should sound historical and grounded.
                    """;

            case MODERN_GRIMDARK -> """
                        Lovecraftian horror setting in the 1920s.
                        Themes: occult, madness, hidden knowledge.
                        Factions are secret societies, cults, investigators.
                        Names should feel mysterious or unsettling.
                    """;

            case CYBERPUNK -> """
                        Futuristic dystopian setting.
                        Themes: megacorporations, hackers, cybernetics.
                        Factions are gangs, corporations, AI groups.
                        Names should feel modern, edgy or corporate.
                    """;

            default -> "Generic setting.";
        };
    }

    private FactionContext randomizeContext() {
        FactionContext[] values = FactionContext.values();
        return values[new Random().nextInt(values.length)];
    }

    private TypeFaction randomizeType() {
        TypeFaction[] values = TypeFaction.values();
        return values[new Random().nextInt(values.length)];
    }

    private String buildCorrectionPrompt(String invalidJson, List<String> errors) {
        return """
                You are a JSON correction engine.
                Fix the following JSON.
                Errors:
                %s

                JSON:
                %s

                Rules:
                - Return ONLY valid JSON
                - Do not add explanations
                - Keep original intent
                """.formatted(String.join("\n", errors), invalidJson);
    }

    private String decideNextPrompt(AgentState agentState, String originalJson) {
        List<String> errors = agentState.getLastErrors();

        // correction si erreur de JSON/parsing
        if (errors.stream().anyMatch(e -> e.contains("json"))) {
            return buildCorrectionPrompt(originalJson, errors);
        }

        return buildRegenerationPrompt(errors);
    }

    private String buildRegenerationPrompt(List<String> errors) {
        return """
                You are a strict JSON generator.
                Previous attempt had these errors:
                - %s

                You MUST fix these issues.
                Rules:
                - Return ONLY valid JSON
                - Respect all constraints strictly
                - Do not repeat previous mistakes

                JSON format:
                {
                "name": "string",
                "typeFaction": "string",
                "objectif": "string",
                "desc": "string"
                }
                """.formatted(String.join("\n- ", errors));
    }

    // #endregion

}

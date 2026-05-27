package com.faction.faction_agent.services;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.faction.faction_agent.enums.AgentAction;
import com.faction.faction_agent.enums.FactionContext;
import com.faction.faction_agent.enums.TypeFaction;
import com.faction.faction_agent.llm.OllamaClient;
import com.faction.faction_agent.models.AgentState;
import com.faction.faction_agent.models.FactionDraft;
import com.faction.faction_agent.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service responsable de la génération de factions via un LLM (Ollama),
 * avec différents niveaux de fiabilisation et de contrôle.
 *
 * <p>
 * Ce service implémente plusieurs stratégies de génération :
 * </p>
 *
 * <ul>
 * <li><b>Génération brute</b> : appel direct au LLM sans validation</li>
 * <li><b>Pipeline fiable</b> : parsing + validation + retry</li>
 * <li><b>Pipeline guidé</b> : correction dynamique basée sur les erreurs</li>
 * </ul>
 *
 * <p>
 * Il ne s'agit pas d'un système agentique complet :
 * la logique de décision (retry, correction, régénération) est pilotée par le
 * code Java,
 * et non par le LLM lui-même.
 * </p>
 *
 * <p>
 * Ce service constitue une base robuste pour :
 * </p>
 * <ul>
 * <li>Génération de contenu structuré (JSON)</li>
 * <li>Validation métier côté backend</li>
 * <li>Itérations contrôlées avec feedback au modèle</li>
 * </ul>
 *
 * <p>
 * Une évolution possible consiste à déléguer la prise de décision au LLM
 * (via un {@code DecisionService}) pour construire un système agentique
 * complet.
 * </p>
 */
@Service
public class FactionGenerationService {

    private final AgentOrchestratorService agentOrchestratorService;

    private final OllamaClient ollamaClient;

    private final DecisionService decisionService;

    private final ObjectMapper objectMapper;

    public FactionGenerationService(
            OllamaClient ollamaClient,
            DecisionService decisionService,
            ObjectMapper objectMapper, AgentOrchestratorService agentOrchestratorService) {
        this.ollamaClient = ollamaClient;
        this.decisionService = decisionService;
        this.objectMapper = objectMapper;
        this.agentOrchestratorService = agentOrchestratorService;
    }

    // #region public Methods

    // =========================================================
    // ================ MÉTHODES PUBLIQUES =====================
    // =========================================================

    /**
     * Génère une faction sous forme de JSON brut via un prompt strict.
     *
     * <p>
     * Aucun mécanisme de validation ou de correction n’est appliqué.
     * Cette méthode est utile pour :
     * </p>
     * <ul>
     * <li>Tester la qualité du prompt</li>
     * <li>Observer le comportement direct du LLM</li>
     * </ul>
     *
     * @return une chaîne JSON (non garantie valide)
     */
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

    /**
     * Génère une faction valide via un pipeline simple avec retry.
     *
     * <p>
     * Fonctionnement :
     * </p>
     * <ul>
     * <li>Appel du LLM</li>
     * <li>Parsing JSON vers {@link FactionDraft}</li>
     * <li>Validation métier</li>
     * <li>Retry jusqu’à un nombre maximal de tentatives</li>
     * </ul>
     *
     * <p>
     * Ce pipeline est robuste mais entièrement piloté par le code.
     * Le LLM ne reçoit pas de feedback sur ses erreurs.
     * </p>
     *
     * @return une faction valide
     * @throws RuntimeException si le LLM échoue après plusieurs tentatives
     */
    public FactionDraft generateFaction() {
        int maxAttempts = 3;
        int attempts = 0;
        while (attempts < maxAttempts) {
            attempts++;
            String json = generateFactionRaw();
            try {
                FactionDraft faction = objectMapper.readValue(json, FactionDraft.class);

                if (isValid(faction)) {
                    return faction;
                }
            } catch (Exception e) {
                System.out.println("Parsing failed (attempt " + attempts + ")");
            }
        }
        throw new RuntimeException("LLM failed after " + maxAttempts + " attempts");
    }

    /**
     * Génère une faction en utilisant un pipeline guidé par les erreurs.
     *
     * <p>
     * Fonctionnement :
     * </p>
     * <ul>
     * <li>Génération initiale avec type et contexte aléatoires</li>
     * <li>Validation métier détaillée</li>
     * <li>Stockage des erreurs dans {@link AgentState}</li>
     * <li>Génération d’un nouveau prompt basé sur les erreurs</li>
     * <li>Correction ou régénération via le LLM</li>
     * </ul>
     *
     * <p>
     * Ce mécanisme introduit une boucle de feedback,
     * mais la stratégie reste déterminée côté Java.
     * </p>
     *
     * <p>
     * Ce pipeline constitue une étape intermédiaire vers un système agentique.
     * </p>
     *
     * @return une faction valide et cohérente avec son contexte
     * @throws RuntimeException si le LLM échoue après plusieurs tentatives
     */
    public FactionDraft generateRandomLedFaction() {
        int maxAttempts = 3;
        AgentState agentState = new AgentState();

        String json = generateLedFactionRaw(randomizeType(), randomizeContext());

        return agentOrchestratorService.runLoop(json, maxAttempts);

        // while (agentState.getAttempts() < maxAttempts) {
        //     agentState.incrementAttempts();
        //     agentState.setLastJson(json);
            
        //     try {
        //         FactionDraft faction = objectMapper.readValue(json, FactionDraft.class);
        //         ValidationResult validation = validate(faction);

        //         if (validation.isValid()) {
        //             System.out.println("==== Faction is valid ! ====");
        //             return faction;
        //         }

        //         agentState.setLastErrors(validation.getErrors());

        //         System.out.println("VALIDATION FAILED");
        //         System.out.println("ERRORS = " + agentState.getLastErrors());

        //         String nextPrompt = decideNextPrompt(agentState, json);
        //         json = ollamaClient.generate(nextPrompt);

        //     } catch (Exception e) {
        //         System.out.println("PARSE FAILED");
        //         agentState.setLastErrors(List.of("Invalid JSON format"));

        //         String nextPrompt = decideNextPrompt(agentState, json);
        //         json = ollamaClient.generate(nextPrompt);
        //     }
        // }

        throw new RuntimeException("LLM failed after " + maxAttempts + " attempts");
    }

    /**
     * Génère une faction brute en imposant un type et un contexte narratif.
     *
     * @param type    type de faction imposé
     * @param context contexte narratif
     * @return JSON brut généré par le LLM
     */
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

    // =========================================================
    // ================ MÉTHODES PRIVÉES =======================
    // =========================================================

    private boolean isValidTypeFaction(String type) {
        try {
            TypeFaction.valueOf(type);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie la validité globale d’une faction (version simple).
     */
    private boolean isValid(FactionDraft faction) {
        return faction.getName() != null && !faction.getName().isBlank()
                && faction.getObjectif() != null && !faction.getObjectif().isBlank()
                && faction.getTypeFaction() != null && !faction.getTypeFaction().isBlank()
                && faction.getDesc() != null && !faction.getDesc().isBlank() && faction.getDesc().length() <= 300
                && isValidTypeFaction(faction.getTypeFaction());
    }

    /**
     * Validation détaillée d’une faction avec retour d’erreurs.
     *
     * @param faction faction à valider
     * @return résultat contenant la liste des erreurs
     */
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

    /**
     * Génère un prompt de correction à partir d’un JSON invalide.
     */
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

    /**
     * Détermine le prochain prompt à envoyer au LLM
     * en fonction des erreurs détectées.
     *
     * <p>
     * Actuellement piloté par des règles simples côté Java :
     * </p>
     * <ul>
     * <li>Erreur JSON → correction</li>
     * <li>Erreur métier → régénération</li>
     * </ul>
     */
    private String decideNextPrompt(AgentState agentState, String originalJson) {
        AgentAction action = decisionService.decide(agentState);
        System.out.println("=== AGENT DECISION === " + action);
        System.out.println("=== ERRORS === " + agentState.getLastErrors());
        System.out.println("=== LAST JSON === " + agentState.getLastJson());
        return switch (action) {
            case FIX_JSON -> buildCorrectionPrompt(originalJson, agentState.getLastErrors());
            case REGENERATE -> buildRegenerationPrompt(agentState.getLastErrors());
            case IMPROVE_CONTENT -> buildImprovementPrompt(originalJson);
            case STOP -> throw new RuntimeException("Agent decided to stop");
        };
    }

    private String buildImprovementPrompt(String json) {
        return """
                Improve the following JSON content.

                Keep structure identical.
                Keep all fields.
                Improve coherence and richness.

                JSON:
                %s

                Rules:
                - Return ONLY valid JSON
                - Do not change structure
                """.formatted(json);
    }

    /**
     * Génère un prompt de régénération en listant les erreurs à corriger.
     */
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

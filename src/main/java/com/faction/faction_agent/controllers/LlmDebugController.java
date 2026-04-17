package com.faction.faction_agent.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.faction.faction_agent.llm.OllamaClient;
import com.faction.faction_agent.models.Decision;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Contrôleur dédié au deboguage du comportement brut du LLM
 * 
 * <p>
 * Il ne fait pas partie du pipeline-métier, c'est un bac à sable d'observation.
 * </p>
 */

@RestController
@RequestMapping("/test")
@Tag(name = "LLM Raw", description = "LLM debugging and validation endpoints")
@CrossOrigin(origins = "http://localhost:4200")
public class LlmDebugController {
    private final OllamaClient ollamaClient;

    public LlmDebugController(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    /**
     * Envoie un prompt minimaliste au LLM demandant une réponse JSON stricte,
     * puis tente de parser la réponse selon plusieurs stratégies.
     *
     * <p>
     * Ce test ne repose sur aucun mécanisme de fiabilisation avancé :
     * pas de retry, pas de validation métier, pas de correction automatique.
     * </p>
     *
     * <p>
     * Stratégies de parsing testées :
     * <ul>
     * <li><b>DIRECT</b> : mapping direct vers l'objet {@link Decision}</li>
     * <li><b>NESTED</b> : extraction depuis un champ "response" (cas fréquent avec
     * certains modèles)</li>
     * <li><b>PARTIAL</b> : extraction partielle des champs "action" et
     * "reason"</li>
     * <li><b>FAILED</b> : échec complet avec retour brut pour analyse</li>
     * </ul>
     * </p>
     *
     * <p>
     * Ce endpoint permet de :
     * <ul>
     * <li>Mesurer la robustesse du modèle face à des contraintes strictes</li>
     * <li>Identifier les formats de réponse inattendus</li>
     * <li>Valider ou ajuster les stratégies de parsing</li>
     * </ul>
     * </p>
     *
     * @return une chaîne indiquant le type de parsing réussi (DIRECT, NESTED,
     *         PARTIAL, FAILED)
     *         ainsi que les données extraites ou les données brutes en cas d’échec
     */
    @GetMapping("/ollama")
    @Operation(summary = "Test LLM JSON response", description = "Sends a strict JSON prompt to the LLM and attempts multiple parsing strategies (direct, nested, partial). Used to debug LLM reliability and response structure.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "LLM response processed and interpreted"),
            @ApiResponse(responseCode = "500", description = "Unexpected error during parsing or LLM call")
    })
    public String testOllama() {
        String prompt = """
                Respond ONLY with JSON:
                {
                  "action": "FIX_JSON",
                  "reason": "test"
                }
                """;
        String raw = ollamaClient.generate(prompt);
        String cleaned = extractJson(raw);

        if (cleaned == null) {
            return "No JSON found in response:\n" + raw;
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            Decision decision = mapper.readValue(cleaned, Decision.class);
            return "[DIRECT] " + decision.getAction() + " | " + decision.getReason();
        } catch (Exception e) {
            try {
                JsonNode root = mapper.readTree(cleaned);

                if (root.has("response")) {
                    JsonNode nested = root.get("response");
                    Decision decision = mapper.treeToValue(nested, Decision.class);
                    return "[NESTED] " + decision.getAction() + decision.getReason();
                }

                if (root.has("action") && root.get("action").isString()) {
                    String action = root.get("action").asString();
                    String reason = root.has("reason") ? root.get("reason").asString() : "no reason";
                    return "[PARTIAL] " + action + " | " + reason;
                }
            } catch (Exception ignored) {
            }
            return "[FAILED]\nRAW " + raw + "\n CLEANED : \n " + cleaned;
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf("{");
        int end = response.indexOf("}");

        if (start == -1) {
            return null;
        }
        if (end == -1 || end < start) {
            return response.substring(start) + "}";
        }

        return response.substring(start, end + 1);
    }
}

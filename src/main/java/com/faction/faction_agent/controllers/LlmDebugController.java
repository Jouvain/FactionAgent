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
 * Controleur dedie au deboguage du comportement brut du LLM.
 *
 * <p>Il ne fait pas partie du pipeline metier; c'est un bac a sable d'observation.</p>
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
     * Envoie un prompt minimaliste au LLM demandant une reponse JSON stricte,
     * puis tente de parser la reponse selon plusieurs strategies.
     *
     * <p>
     * Ce test ne repose sur aucun mecanisme de fiabilisation avance:
     * pas de retry, pas de validation metier, pas de correction automatique.
     * </p>
     *
     * <p>Strategies de parsing testees:</p>
     * <ul>
     * <li><b>DIRECT</b> : mapping direct vers l'objet {@link Decision}</li>
     * <li><b>NESTED</b> : extraction depuis un champ "response" (cas frequent avec
     * certains modeles)</li>
     * <li><b>PARTIAL</b> : extraction partielle des champs "action" et
     * "reason"</li>
     * <li><b>FAILED</b> : echec complet avec retour brut pour analyse</li>
     * </ul>
     *
     * <p>Ce endpoint permet de:</p>
     * <ul>
     * <li>Mesurer la robustesse du modele face a des contraintes strictes</li>
     * <li>Identifier les formats de reponse inattendus</li>
     * <li>Valider ou ajuster les strategies de parsing</li>
     * </ul>
     *
     * @return une chaine indiquant le type de parsing reussi (DIRECT, NESTED,
     *         PARTIAL, FAILED) ainsi que les donnees extraites ou les donnees
     *         brutes en cas d'echec
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

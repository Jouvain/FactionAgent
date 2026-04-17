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

@RestController
@RequestMapping("/test")
@Tag(name = "Test", description = "LLM debugging and validation endpoints")
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {
    private final OllamaClient ollamaClient;

    public TestController(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

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

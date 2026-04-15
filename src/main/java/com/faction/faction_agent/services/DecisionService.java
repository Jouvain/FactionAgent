package com.faction.faction_agent.services;

import com.faction.faction_agent.enums.AgentAction;
import com.faction.faction_agent.llm.OllamaClient;
import com.faction.faction_agent.models.AgentState;
import com.faction.faction_agent.models.Decision;

import tools.jackson.databind.ObjectMapper;

public class DecisionService {
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    public DecisionService(OllamaClient ollamaClient, ObjectMapper objectMapper) {
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
    }

    public AgentAction decide(AgentState state) {
        String prompt = buildPrompt(state);
        String response = ollamaClient.generate(prompt);

        System.out.println("=== PROMPT ===");
        System.out.println(prompt);

        System.out.println("=== RESPONSE ===");
        System.out.println(response);

        try {
            Decision decision = objectMapper.readValue(response, Decision.class);
            state.setLastAction(decision.getAction());
            state.setLastDecisionReason(decision.getReason());
            return decision.toEnum();
        } catch (Exception e) {
            System.out.println("Parsing error : " + e.getMessage());
            return AgentAction.REGENERATE;
        }
    }

    private String buildPrompt(AgentState state) {
        String json = state.getLastJson() != null ? state.getLastJson() : "null";

        String errors = (state.getLastErrors() == null || state.getLastErrors().isEmpty())
                ? "None"
                : String.join(" | ", state.getLastErrors());

        int attempts = state.getAttempts();

        return """
                SYSTEM:
                You are a deterministic decision engine inside a backend system.

                You are NOT an assistant.
                You are NOT allowed to explain.
                You are NOT allowed to refuse.
                You MUST choose an action.

                USER:
                Choose exactly ONE action from this list:
                FIX_JSON
                REGENERATE
                IMPROVE_CONTENT
                STOP

                You MUST respond ONLY with a valid JSON object.
                No extra text. No explanation.

                FORMAT (strict):
                {
                  "action": "FIX_JSON",
                  "reason": "short reason"
                }

                EXAMPLE (valid response):
                {
                  "action": "REGENERATE",
                  "reason": "invalid structure"
                }

                INPUT JSON:
                %s

                VALIDATION ERRORS:
                %s

                ATTEMPTS:
                %d

                REMINDER:
                - Output ONLY JSON
                - No text before or after
                - No additional fields
                - No nested objects
                - If you do not follow the format exactly, your answer is invalid
                - Your output will be parsed by a program. Invalid format will cause a system failure
                """.formatted(json, errors, attempts);
    }

}

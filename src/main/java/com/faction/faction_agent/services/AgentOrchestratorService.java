package com.faction.faction_agent.services;

import org.springframework.stereotype.Service;

import com.faction.faction_agent.models.AgentState;
import com.faction.faction_agent.models.FactionDraft;



@Service
public class AgentOrchestratorService {

    public FactionDraft runLoop(String initialJson, int maxSteps) {
        AgentState agentState = new AgentState();
        while (agentState.getAttempts() < maxSteps && !agentState.isValid()) {
            System.out.println("---- STEP " + agentState.getAttempts() + " ----");
            
            // observe(agentState)
            // Decision decision = decide(agentState)
            // act(decision, agentState)

            agentState.incrementAttempts();
        }
    }

}

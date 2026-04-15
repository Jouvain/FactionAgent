package com.faction.faction_agent.models;

import com.faction.faction_agent.enums.AgentAction;

public class Decision {
    private String action;
    private String reason;

    public Decision() {
    }

    public Decision(String action, String reason) {
        this.action = action;
        this.reason = reason;
    }

    public AgentAction toEnum() {
        try {
            return AgentAction.valueOf(action);
        } catch (Exception e) {
            return AgentAction.REGENERATE;
        }
        
    }

    public String getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }
}

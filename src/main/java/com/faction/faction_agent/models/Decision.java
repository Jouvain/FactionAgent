package com.faction.faction_agent.models;

import com.faction.faction_agent.enums.AgentAction;

public class Decision {
    private String action;
    private String reason;

    public Decision() {}

    public Decision(String action, String reason) {
        this.action = action;
        this.reason = reason;
    }

    public AgentAction toEnum() {
        return AgentAction.valueOf(action);
    }

    public String getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }
}

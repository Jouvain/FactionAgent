package com.faction.faction_agent.models;

import java.util.ArrayList;
import java.util.List;

public class AgentState {
    private int attempts;
    private String lastJson;
    private List<String> lastErrors;

    public AgentState() {
        this.attempts = 0;
        this.lastErrors = new ArrayList<>();
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public String getLastJson() {
        return lastJson;
    }

    public void setLastJson(String lastJson) {
        this.lastJson = lastJson;
    }

    public List<String> getLastErrors() {
        return lastErrors;
    }

    public void setLastErrors(List<String> errors) {
        this.lastErrors = new ArrayList<>(errors);
    }
}

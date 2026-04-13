package com.faction.faction_agent.models;

import java.util.ArrayList;
import java.util.List;

public class AgentState {
    private int attempts;
    private String lastJson;
    private List<String> lastErrors;

    private String lastAction;
    private boolean valid;
    private String lastDecisionReason;


    public AgentState() {
        this.attempts = 0;
        this.lastErrors = new ArrayList<>();
        this.valid = false;
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

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public boolean isValid() {
        return valid;
    }

    public String getLastDecisionReason() {
        return lastDecisionReason;
    }

    public void setLastDecisionReason(String reason) {
        this.lastDecisionReason = reason;
    }

    public boolean hasErrors() {
        return lastErrors != null && !lastErrors.isEmpty();
    }

    public void resetErrors() {
        this.lastErrors.clear();
        this.valid = true;
    }

}

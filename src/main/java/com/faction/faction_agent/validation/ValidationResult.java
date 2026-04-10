package com.faction.faction_agent.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private boolean valid;
    private List<String> errors;

    public ValidationResult() {
        this.valid = true; 
        this.errors = new ArrayList<>();
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        this.errors.add(error);
        this.valid = false; 
    }

    public void setValid(boolean bool) {
        this.valid = bool;
    }
}

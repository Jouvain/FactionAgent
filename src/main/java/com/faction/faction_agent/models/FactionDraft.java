package com.faction.faction_agent.models;

public class FactionDraft {
    private String name;
    private String typeFaction;
    private String objectif;


    public FactionDraft() {}

    public FactionDraft(String name, String typeFaction, String objectif) {
        this.name = name;
        this.typeFaction = typeFaction;
        this.objectif = objectif;
    }

    public String getName() {
        return name;
    }
    public String getTypeFaction() {
        return typeFaction;
    }
    public String getObjectif() {
        return objectif;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setTypeFaction(String typeFaction) {
        this.typeFaction = typeFaction;
    }
    public void setObjectif(String objectif) {
        this.objectif = objectif;
    }



}

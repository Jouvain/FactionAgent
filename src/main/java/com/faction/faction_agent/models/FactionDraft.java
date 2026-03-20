package com.faction.faction_agent.models;

public class FactionDraft {
    private String name;
    private String typeFaction;
    private String objectif;
    private String desc;


    public FactionDraft() {}

    public FactionDraft(String name, String typeFaction, String objectif, String desc) {
        this.name = name;
        this.typeFaction = typeFaction;
        this.objectif = objectif;
        this.desc = desc;
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
    public String getDesc() {
        return desc;
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
    public void setDesc(String desc) {
        this.desc = desc;
    }



}

package com.faction.faction_agent.models;

import io.swagger.v3.oas.annotations.media.Schema;

public class FactionDraft {

    @Schema(example = "Le Cercle des Cendres", description = "Faction name")
    private String name;

    @Schema(example = "SECTE_CULTE", description = "Faction typeFaction")
    private String typeFaction;

    @Schema(example = "Invoquer la Mère des Pleurs Noirs", description = "Faction objectif")
    private String objectif;

    @Schema(
        example = "Une secte discrète, opérant dans les bas-fonds de Vives-Aigues",
        description = "Description détaillée"
    )    
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

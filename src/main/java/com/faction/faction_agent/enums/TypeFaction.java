package com.faction.faction_agent.enums;

public enum TypeFaction {
	SECTE_CULTE("Secte <-> Culte"),
	CABALE_GOUV("Cabale <-> Gouvernement"),
	MAFIA_GUILDE("Mafia <-> Guilde"),
	HORDE_LEGION("Horde <-> Légion"),
	CLAN_DYNASTIE("Clan <-> Dynastie"),
	BANDE_COMPAGNIE("Bande <-> Compagnie");
	
	private final String label;
	
	TypeFaction(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}

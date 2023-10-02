package api.souls;

import java.util.HashMap;

import api.units.UnitRarity;


public enum SoulType {

	ARCHER_SOUL(UnitRarity.COMMON, "Archer Soul", "archer"),
	FLAGBEARER_SOUL(UnitRarity.COMMON, "Flag Bearer Soul", "flagbearer"),
	ROGUE_SOUL(UnitRarity.COMMON, "Rogue Soul", "rogue"),
	TANK_SOUL(UnitRarity.COMMON, "Tank Soul", "tank"),
	WARRIOR_SOUL(UnitRarity.COMMON, "Warrior Soul", "warrior")
	;

	public final UnitRarity quality;
	public final String name, unitCurrencyType;

	private SoulType(UnitRarity quality, String name, String unitCurrencyType) {
		this.quality = quality;
		this.name = name;
		this.unitCurrencyType = unitCurrencyType;
	}

	private final static HashMap<String, SoulType> from_unitType;
	static {
		from_unitType = new HashMap<>();
		for (SoulType value : values())
			from_unitType.put(value.unitCurrencyType, value);

	}

	public static SoulType parseUnit(String unitType) {
		return from_unitType.get(unitType);
	}

}

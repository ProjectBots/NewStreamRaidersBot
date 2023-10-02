package api.units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import otherlib.Cache;

public class UnitType implements Comparable<UnitType>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return utid;
	}

	@Override
	public int compareTo(UnitType ut) {
		int t = this.rarity.rank - ut.rarity.rank;
		if (t != 0)
			return t;

		t = this.name.compareTo(ut.name);
		if (t != 0)
			return t;

		return this.utid.compareTo(ut.utid);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof UnitType))
			return false;

		//	if same id, it is the same unit type
		return utid.equals(((UnitType) obj).utid);
	}

	// would really love to see some enum action
	// unfortunately, sometimes a new unitType has to be added during Runtime
	private static HashMap<String, UnitType> unitTypes;
	private static List<String> unitTypeIds;

	/**
	 * sr is sometimes a bit inconsistent with unit type ids.<br>
	 * this method remaps strays to their real id
	 * @param in
	 * @return the resulting unitTypeId
	 */
	public static String unitTypeStringRemap(String in) {
		switch (in) { // ¯\_(ツ)_/¯
		case "paladin":
			return "alliespaladin";
		case "balloonbuster":
			return "alliesballoonbuster";
		case "flyingrogue":
			return "flyingarcher";
		default:
			return in;
		}
	}

	/**
	 * returns the UnitType Object associated with the given type
	 * 
	 * @param type
	 * @return UnitType Object
	 */
	public static UnitType getUnitType(String type) {
		return unitTypes.get(unitTypeStringRemap(type));
	}

	/**
	 * @return a collection with all UnitType Objects
	 */
	public static Collection<UnitType> getUnitTypes() {
		return unitTypes.values();
	}

	/**
	 * @return an {@link Collections#unmodifiableList(List) unmodifiableList}
	 *         containing every unitType id
	 */
	public static List<String> getUnitTypeIds() {
		return unitTypeIds;
	}

	/**
	 * @return the amount of unit types that exist
	 */
	public static int amount() {
		return unitTypeIds.size();
	}

	/**
	 * initializes unit types
	 */
	public static void ini() {
		if ((unitTypes = Cache.remove("unitTypes")) == null)
			unitTypes = new HashMap<String, UnitType>();

		UnitType.unitTypeIds = Collections.unmodifiableList(new ArrayList<>(unitTypes.keySet()));
	}

	/**
	 * stores all unit types in cache for saving
	 */
	public static void save() {
		Cache.put("unitTypes", unitTypes);
	}

	/**
	 * updates all unit types and adds new ones.<br>
	 * also updates specializations.
	 * @param data from sr's datapath
	 */
	public static void genUnitTypesFromData(JsonObject data) {
		JsonObject us = data.getAsJsonObject("Units");
		boolean addedType = false;
		for (String key : us.keySet()) {
			JsonObject u = us.getAsJsonObject(key);

			if (!u.get("PlacementType").getAsString().equals("viewer"))
				continue;

			String type = u.get("UnitType").getAsString();

			if (!unitTypes.containsKey(type)) {
				unitTypes.put(type,
						new UnitType(type, u.get("DisplayName").getAsString(), u.get("IsFlying").getAsBoolean(),
								UnitRole.valueOf(u.get("Role").getAsString().toUpperCase()),
								UnitRarity.valueOf(u.get("Rarity").getAsString().toUpperCase())));

				addedType = true;
			}

			int level = u.get("Level").getAsInt();
			short power = u.get("Power").getAsShort();

			unitTypes.get(type).updatePower(level, power);
		}
		
		if (addedType)
			UnitType.unitTypeIds = Collections.unmodifiableList(new ArrayList<>(unitTypes.keySet()));
		
		JsonObject sps = data.getAsJsonObject("Specialization");
		ArrayList<String> sids = new ArrayList<>(sps.keySet());
		//	ensures that specs are always the same order
		sids.sort(null);
		for (String sid : sids) {
			if (sid.startsWith("epic") || sid.startsWith("versus") || sid.startsWith("captain") || sid.startsWith("cpatain"))// ¯\_(ツ)_/¯ typo by sr
				continue;

			JsonObject sp = sps.getAsJsonObject(sid);

			unitTypes.get(sp.get("UnitType").getAsString()).addSpec(sid, sp.get("DisplayName").getAsString());
		}
	}

	/**
	 * parses a character type (ex: epicbomber30) into its unitType (ex: bomber)
	 * 
	 * @param ct
	 * @return the resulting unit type
	 */
	public static String getUnitTypeFromCharacterType(String ct) {
		String ret = ct.replaceAll("\\d+$|^captain|^epic", "");
		ret = unitTypeStringRemap(ret);
		return unitTypes.containsKey(ret) ? ret : null;
	}

	public final String utid, name, ptag;
	public final UnitRarity rarity;
	public final boolean canFly;
	public final UnitRole role;

	private final String[] specIds = new String[3];
	private final String[] specNames = new String[3];

	private final short[] power = new short[19];

	private UnitType(String utid, String name, boolean canFly, UnitRole role, UnitRarity rarity) {
		this.utid = utid;
		this.name = name;
		this.rarity = rarity;
		this.canFly = canFly;
		this.role = role;
		this.ptag = name.toLowerCase().replace(" ", "");
	}

	/**
	 * checks if this unit type is suitable for the given placement tag
	 * @param ptag
	 * @return true / false
	 */
	public boolean hasPTag(String ptag) {
		return role.toString().toLowerCase().equals(ptag) || utid.equals(ptag) || "vibe".equals(ptag);
	}

	/**
	 * returns the nth specialization id for this unit type.<br>
	 * specialization ids are always sorted alphanumerical.
	 * @param n 0; 1; 2
	 * @return the nth specialization id
	 */
	public String getSpecId(int n) {
		//	will be sorted during generation
		return specIds[n];
	}

	/**
	 * returns the nth specialization name for this unit type.<br>
	 * specialization names are ordered in a way that {@code n} matches up with {@link #getSpecId(int) getSpecId}'s {@code n}
	 * @param n 0; 1; 2
	 * @return the nth specialization name 
	 */
	public String getSpecName(int n) {
		//	will be sorted during generation
		return specNames[n];
	}

	/**
	 * adds a specialization to this unit type
	 * @param sid
	 * @param name
	 */
	private void addSpec(String sid, String name) {
		for (int i = 0; i < 3; i++) {
			if (specIds[i] == null) {
				specIds[i] = sid;
				specNames[i] = name;
				return;
			}
			if(sid.equals(specIds[i]))
				return;
		}
		throw new RuntimeException("too many specializations, tried to add "+sid+" to "+this.utid);
	}
	
	/**
	 * @param level
	 * @return the power of this unit for the given level
	 */
	public short getPower(int level) {
		return this.power[level];
	}

	/**
	 * updates the power of this unit for the given level
	 * @param level
	 * @param power
	 */
	private void updatePower(int level, short power) {
		this.power[level] = power;
	}
}

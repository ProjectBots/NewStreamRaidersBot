package api.souls;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import api.units.Unit;

public class Soul {
	
	@Override
	public String toString() {
		return new StringBuffer("{")
				.append(type.toString()).append(" ")
				.append(soulId).append("}").toString();
	}

	public final SoulType type;
	public final int soulId;
	
	private int unitId;
	
	public Soul(JsonObject soul) {
		this.soulId = soul.get("soulId").getAsInt();
		JsonElement je = soul.get("unitId");
		this.unitId = je.isJsonPrimitive() ? je.getAsInt() : -1;
		this.type = SoulType.valueOf(soul.get("soulType").getAsString().toUpperCase());
	}
	
	/**
	 * updates the unitId value.
	 * @param u
	 * @see #getUnitId()
	 */
	public void setUnit(Unit u) {
		this.unitId = u != null ? u.unitId : -1;
	}
	
	/**
	 * @return the id of the unit this soul is linked to or -1 if none
	 */
	public int getUnitId() {
		return unitId;
	}

	
	
}

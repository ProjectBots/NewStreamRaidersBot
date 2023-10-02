package api.skins;

import java.io.Serializable;

import com.google.gson.JsonObject;

import api.units.UnitType;

public class Skin implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Override
	public String toString() {
		return "{uid=" + sid + ", captainId=" + captainId + ", disname=" + disname + ", unit=" + unitType
				+ ", type=" + type + "}";
	}

	@Override
	public boolean equals(Object obj) {
		// if the uid is equal, they are the same
		return this.sid.equals(((Skin) obj).sid);
	}

	public final String sid;
	public final String unitType;
	public final String disname;
	public final SkinType type;
	public final String captainId;

	//	should only be created by Skins
	Skin(JsonObject pack) {
		sid = pack.get("Uid").getAsString();
		unitType = pack.get("BaseUnitType").getAsString();
		disname = pack.get("DisplayName").getAsString();
		type = SkinType.parseString(pack.get("Type").getAsString());
		captainId = pack.get("StreamerId").getAsString();
	}
	
	/**
	 * @return the unit type of this skin
	 */
	public UnitType getUnitType() {
		return UnitType.getUnitType(unitType);
	}
}
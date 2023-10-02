package api.units;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import api.Time;
import api.skins.Skin;
import api.skins.Skins;
import api.souls.Soul;
import api.souls.SoulType;

public class Unit implements Comparable<Unit> {
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
				.append("{").append(getType().toString())
				.append(" ").append(level);
		
		if(soulType != null)
			sb.append(" ").append(soulType.toString());
		
		return sb.append("}").toString();
	}
	
	@Override
	public int compareTo(Unit u) {
		int t = this.getType().compareTo(u.getType());
		if(t != 0)
			return t;
		
		return u.level - this.level;
	}
	
	
	public final int unitId;
	public final String unitTypeId;

	private SoulType soulType;
	private String skin, specId, specName;
	private int level, soulId;
	private long cool;

	public Unit(JsonObject unit) {
		this.unitId = unit.get("unitId").getAsInt();
		this.unitTypeId = unit.get("unitType").getAsString();
		update(unit);
	}
	
	public void update(JsonObject unit) {
		if(this.unitId != unit.get("unitId").getAsInt())
			throw new RuntimeException("unitId mismatch");
		
		this.level = unit.get("level").getAsInt();
		
		JsonElement je = unit.get("cooldownTime");
		this.cool = je.isJsonPrimitive() ? Time.parse(je.getAsString()) + 10 : 0;
		je = unit.get("soulId");
		this.soulId = je.isJsonPrimitive() ? je.getAsInt() : -1;
		je = unit.get("soulType");
		this.soulType = je.isJsonPrimitive() ? SoulType.valueOf(je.getAsString().toUpperCase()) : null;
		je = unit.get("skin");
		this.skin = je.isJsonPrimitive() ? je.getAsString() : null;
		
		je = unit.get("specializationUid");
		if(je.isJsonPrimitive()) {
			this.specId = je.getAsString();
			UnitType ut = getType();
			fl: {
				for(int i=0; i<3; i++) {
					if(specId.equals(ut.getSpecId(i))) {
						this.specName = ut.getSpecName(i);
						break fl;
					}
				}
				throw new RuntimeException("failed to find spec "+specId+" for "+ut.utid);
			}
			
		} else {
			this.specId = null;
			this.specName = null;
		}
	}
	
	public UnitType getType() {
		return UnitType.getUnitType(unitTypeId);
	}
	
	public boolean isAvailable() {
		return Time.isBeforeServerTime(cool);
	}
	
	public void setSoul(Soul soul) {
		this.soulType = soul != null ? soul.type : null;
		this.soulId = soul != null ? soul.soulId : -1;
	}
	
	public SoulType getSoulType() {
		return soulType;
	}
	
	public int getSoulId() {
		return soulId;
	}
	
	public void setSkin(Skin skin) {
		this.skin = skin==null?null:skin.sid;
	}
	
	public String getSkin() {
		return skin;
	}
	
	public String getDisName() {
		if(skin != null)
			return Skins.getSkin(skin).disname;
		
		if(specName != null)
			return specName;
		
		return getType().name;
	}
	
	
}

package api.skins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonObject;

import api.units.UnitType;
import otherlib.Cache;

public class Skins {
	
	private static HashMap<String, Skin> ALL_SKINS;
	
	/**
	 * initializes all skins from cache
	 */
	public static void ini() {
		if((ALL_SKINS = Cache.remove("skins")) == null)
			ALL_SKINS = new HashMap<String, Skin>();
	}
	
	/**
	 * stores all skins to cache for saving
	 */
	public static void save() {
		Cache.put("skins", ALL_SKINS);
	}
	
	public static void genSkinsFromData(JsonObject data) {
		data = data.getAsJsonObject("Skins");
		for(String key : data.keySet()) {
			if(ALL_SKINS.containsKey(key))
				continue;
			ALL_SKINS.put(key, new Skin(data.getAsJsonObject(key)));
		}
	}
	
	/**
	 * @param sid
	 * @return the skin object for the specified id or null if not found
	 */
	public static Skin getSkin(String sid) {
		return ALL_SKINS.get(sid);
	}
	
	/**
	 * creates a Skins object for a specific profile.<br>
	 * it will be initialized from cache ({@code pid+"::skins"})
	 * @param pid
	 * @return resulting Skins object
	 */
	public static Skins iniSkinsForProfile(long pid) {
		return new Skins(pid);
	}
	
	@Override
	public String toString() {
		return skins.toString();
	}
	
	private HashSet<String> skins = new HashSet<>();
	
	private Skins(long pid) {
		if((skins = Cache.remove(pid+"::skins")) == null)
			skins = new HashSet<String>();
	}
	
	/**
	 * @param sid
	 * @return true if this Profile has the specified Skin
	 */
	public boolean hasSkin(String sid) {
		return skins.contains(sid);
	}
	
	/**
	 * adds a Skin to this Profile
	 * @param sid the skin id
	 */
	public void addSkin(String sid) {
		skins.add(sid);
	}
	
	/**
	 * @return a List with the ids of all skins this Profile has
	 */
	public ArrayList<String> getSkinIds() {
		return new ArrayList<>(skins);
	}
	
	/**
	 * searches through all skins this Profile has
	 * @param captainId search for a specific captain with his id
	 * @param unitType search for a specific unitType
	 * @param exclude skinTypes to be excluded
	 * @return a List containing all matching Skin objects
	 */
	public ArrayList<Skin> searchSkins(String captainId, UnitType unitType, SkinType... exclude) {
		ArrayList<Skin> ret = new ArrayList<>();
		for(String sid : skins) {
			Skin skin = getSkin(sid);
			if((captainId == null || skin.captainId.equals(captainId))
					&& (unitType == null || skin.unitType.equals(unitType.utid))
					&& !ArrayUtils.contains(exclude, skin.type))
				ret.add(skin);
		}
		return ret;
	}
	
}

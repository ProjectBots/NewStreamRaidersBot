package api.profiles.viewer;

import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import api.SC;
import api.profiles.Profile;
import api.profiles.RequestHelper;
import api.units.Unit;

public class Viewer {

	public final Profile profile;
	
	//	sorted by unitId in ascending order (basically older ones first)
	private Unit[] units;

	public Viewer(Profile p) {
		this.profile = p;

		RequestHelper rh = profile.getRequestHelper();

		// TODO logging
		// TODO handle data
		rh.getUserUnits(true, raw -> {
			JsonArray data = raw.getAsJsonArray("data");
			int size = data.size();
			units = new Unit[size];
			for(int i=0; i<size;i++) {
				JsonObject u = data.get(i).getAsJsonObject();
				units[i] = new Unit(u);
			}
			Arrays.sort(units, (u1, u2) -> u2.unitId - u1.unitId);
		}, e -> e.printStackTrace());
		rh.getUserSouls(true, raw -> {
		}, e -> e.printStackTrace());
		rh.getCurrentStoreItems(true, raw -> {
		}, e -> e.printStackTrace());
		rh.getUserEventProgression(true, raw -> {
		}, e -> e.printStackTrace());
		rh.getActiveAmbassadors(true, raw -> {
		}, e -> e.printStackTrace());
		rh.grantEventQuestMilestoneReward(true, raw -> {
		}, e -> e.printStackTrace());
		rh.getUserQuests(true, raw -> {
		}, e -> e.printStackTrace());
		rh.getUserItems(true, raw -> {
		}, e -> e.printStackTrace());

		try {
			rh.waitForFinish();
		} catch (InterruptedException e1) {
			// TODO logging
			e1.printStackTrace();
		}

		rh.getActiveRaidsByUser(SC.zeroLengthIntArr, true, raw -> {
		}, e -> e.printStackTrace());
		rh.getAvailableCurrencies(true, raw -> {
		}, e -> e.printStackTrace());
	}

	public void update() {
		// TODO logging
		// TODO handle data
		RequestHelper rh = profile.getRequestHelper();
		rh.getUser(true, raw -> {
		}, e -> e.printStackTrace());
		rh.getFactionInfo(true, raw -> {
		}, e -> e.printStackTrace());
		rh.getAvailableCurrencies(true, raw -> {
		}, e -> e.printStackTrace());
		rh.getCurrentStoreItems(true, raw -> {
		}, e -> e.printStackTrace());
		rh.getOpenCountTrackedChests(true, raw -> {
		}, e -> e.printStackTrace());
		// TODO placement indices
		rh.getActiveRaidsByUser(null, true, raw -> {
		}, e -> e.printStackTrace());
		rh.getLiveAndPlayingCaptainCount(true, raw -> {
		}, e -> e.printStackTrace());
	}

}

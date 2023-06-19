package api.profiles;

import api.SC;
import api.Time;
import api.profiles.captain.Captain;
import api.profiles.viewer.Viewer;

public class Profile {
	

	private ProfileType type;
	private Viewer viewer = null;
	private Captain captain = null;
	
	private RequestHelper rh;
	
	
	public Profile(String cookies, String userAgent, String gameDataVersion, String clientVersion) {
		rh = new RequestHelper(cookies, userAgent, gameDataVersion, clientVersion);
		try {
			//	TODO logging
			rh.ini(e -> e.printStackTrace());
		} catch (InterruptedException e2) {
			//	TODO logging
			e2.printStackTrace();
		}
		
		if(rh.isCaptain()) {
			type = ProfileType.CAPTAIN;
			captain = new Captain();
		} else {
			type = ProfileType.VIEWER;
			viewer = new Viewer();
		}
		
		Time.updateSecsOff(rh.getCurrentTime(false, null, null).get("date").getAsString());
		
		//	TODO logging
		//	TODO handle data
		rh.getUserUnits(true, raw -> {}, e -> e.printStackTrace());
		rh.getUserSouls(true, raw -> {}, e -> e.printStackTrace());
		rh.getCurrentStoreItems(true, raw -> {}, e -> e.printStackTrace());
		rh.getUserEventProgression(true, raw -> {}, e -> e.printStackTrace());
		rh.getActiveAmbassadors(true, raw -> {}, e -> e.printStackTrace());
		rh.grantEventQuestMilestoneReward(true, raw -> {}, e -> e.printStackTrace());
		rh.getUserQuests(true, raw -> {}, e -> e.printStackTrace());
		rh.getUserItems(true, raw -> {}, e -> e.printStackTrace());
		
		try {
			rh.waitForFinish();
		} catch (InterruptedException e1) {
			//	TODO logging
			e1.printStackTrace();
		}
		
		rh.getActiveRaidsByUser(SC.zeroLengthIntArr, true, raw -> {}, e -> e.printStackTrace());
		rh.getAvailableCurrencies(true, raw -> {}, e -> e.printStackTrace());
		
		try {
			rh.waitForFinish();
		} catch (InterruptedException e1) {
			//	TODO logging
			e1.printStackTrace();
		}
		
		
	}
	
	
}

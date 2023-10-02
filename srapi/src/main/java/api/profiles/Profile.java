package api.profiles;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import api.Time;
import api.profiles.captain.Captain;
import api.profiles.viewer.Viewer;

public class Profile {
	

	private ProfileType type;
	private Viewer viewer = null;
	private Captain captain = null;
	
	private RequestHelper rh;
	
	private AtomicInteger time = new AtomicInteger(30);
	private Timer timer = new Timer();
	
	public Profile(String cookies, String userAgent, String gameDataVersion, String clientVersion) {
		rh = new RequestHelper(cookies, userAgent, gameDataVersion, clientVersion);
		try {
			//	TODO logging
			rh.ini(e -> e.printStackTrace());
		} catch (InterruptedException e2) {
			//	TODO logging
			e2.printStackTrace();
			throw new RuntimeException(e2);
		}
		
		Time.updateSecsOff(rh.getCurrentTime(false, null, null).get("date").getAsString());
		
		if(rh.isCaptain()) {
			type = ProfileType.CAPTAIN;
			captain = new Captain(this);
		} else {
			type = ProfileType.VIEWER;
			viewer = new Viewer(this);
		}
		
		try {
			rh.waitForFinish();
		} catch (InterruptedException e1) {
			//	TODO logging
			e1.printStackTrace();
		}
		
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				int t = time.decrementAndGet();
				if(t == 0) {
					switch (type) {
					case CAPTAIN:
						captain.update();
						break;
					case VIEWER:
						viewer.update();
						break;
					}
					time.set(30);
				}
			}
		}, 1000);
		
	}
	
	public RequestHelper getRequestHelper() {
		return rh;
	}
	
	
}

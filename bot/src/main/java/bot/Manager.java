package bot;

import java.io.FileNotFoundException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import api.profiles.Profile;
import bot.config.CProfile;
import bot.config.Config;

public class Manager {

	private long[] pids;
	private Profile[] profiles;

	private ThreadPoolExecutor exec = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

	public Manager() {
		try {
			Config.ini();

			pids = Config.getPIDs();
			profiles = new Profile[pids.length];

			for (int i = 0; i < pids.length; i++) {
				final int ii = i;
				CProfile cpro = Config.getProfile(pids[i]);
				
				exec.execute(() -> profiles[ii] = new Profile(cpro.cookies, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)", "c1fce0b2c334", "0.225.0"));
				
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
}

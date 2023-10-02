package bot.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonObject;

import bot.Ressources;

public class Config {

	
	
	
	public static void ini() throws FileNotFoundException {
		
		File file = new File("data"+File.separator+"config.json");
		System.out.println(file.getAbsolutePath());
		
		JsonObject jcon = Ressources.readFile("data/config.json");

		pids = new long[jcon.size()];
		int i = 0;
		
		for(String key : jcon.keySet()) {
			pids[i] = Long.valueOf(key);
			pros.put(pids[i], new CProfile(jcon.getAsJsonObject(key)));
			i++;
		}
		
	}
	
	private static HashMap<Long, CProfile> pros = new HashMap<>();
	
	public static CProfile getProfile(long pid) {
		return pros.get(pid);
	}
	
	private static long[] pids = null;
	
	public static long[] getPIDs() {
		return Arrays.copyOf(pids, pids.length);
	}
}

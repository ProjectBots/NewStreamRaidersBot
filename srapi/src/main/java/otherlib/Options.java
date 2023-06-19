package otherlib;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonObject;

public class Options {


	private static boolean isUpdatingDataPath = false;
	
	public static boolean isUpdatingDataPath() {
		return isUpdatingDataPath;
	}
	
	private static String currentDataPath = "";

	public static String getCurrentDataPath() {
		return currentDataPath;
	}
	
	public static synchronized void update(String dataPath, JsonObject data) {
		if(dataPath.equals(currentDataPath))
			return;
		isUpdatingDataPath = true;
		currentDataPath = dataPath;
		//	TODO
		
		
		isUpdatingDataPath = false;
	}

	
	
}

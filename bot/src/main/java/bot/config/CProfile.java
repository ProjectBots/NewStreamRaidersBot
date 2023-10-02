package bot.config;

import com.google.gson.JsonObject;

public class CProfile {

	
	public final String cookies;
	public final String name;
	
	public CProfile(JsonObject jpro) {
		name = jpro.get("name").getAsString();
		cookies = jpro.get("cookies").getAsString();
	}

}

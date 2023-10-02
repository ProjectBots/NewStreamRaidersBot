package otherlib;

import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Json Parser<br>
 * contains helper methods to parse json's
 * @author ProjectBots
 *
 */
public class JP {

	//	reuse instances of gson to save ressources
	private static final Gson normal = new Gson();
	private static final Gson pretty = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	
	public static JsonObject parseObj(String json) {
		return normal.fromJson(json, JsonObject.class);
	}
	
	public static JsonObject parseObj(Reader json) {
		return normal.fromJson(json, JsonObject.class);
	}
	
	public static JsonArray parseArr(String json) {
		return normal.fromJson(json, JsonArray.class);
	}
	
	public static JsonElement parse(String json) {
		return normal.fromJson(json, JsonElement.class);
	}

	/**
	 * <b>slow!</b><br>
	 * creates a json representation of any object
	 * @param in any objoect
	 * @return resulting json
	 * @see Gson#toJsonTree(Object)
	 */
	public static JsonElement fromObj(Object in) {
		return normal.toJsonTree(in);
	}
	
	public static <T>T toObj(JsonElement in, Class<T> c) {
		return normal.fromJson(in, c);
	}
	
	public static String prettyJson(JsonElement json) {
		return pretty.toJson(json);
	}
}

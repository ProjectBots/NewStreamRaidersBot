package test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.google.gson.JsonObject;

import api.skins.Skin;
import otherlib.JP;

public class SkinTests {

	private static Skin testArcher = null;
	
	public static Skin getTestSkin() {
		if(testArcher == null) {
			//	constructor is "default"
			//	using reflection to access it
			try {
				Constructor<Skin> constructor = Skin.class.getDeclaredConstructor(JsonObject.class);
				constructor.setAccessible(true);
				testArcher = constructor.newInstance(JP.parseObj("{\"Uid\":\"testarcher\",\"BaseUnitType\":\"archer\",\"DisplayName\":\"TestArcher\",\"Type\":\"Epic\",\"StreamerId\":\"133769420c\"}"));
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
			
		}
		
		return testArcher;
	}
	
			
}

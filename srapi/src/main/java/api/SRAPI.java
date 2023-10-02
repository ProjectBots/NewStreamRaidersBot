package api;

import java.io.IOException;

import api.skins.Skins;
import api.units.UnitType;
import otherlib.Cache;

public class SRAPI {
	
	public static void ini() throws ClassNotFoundException, IOException {
		
		Cache.load("data/cache.data");

		UnitType.ini();
		Skins.ini();
		
	}

	public static void end() throws IOException {
		//	TODO wait for profiles to finish
		
		UnitType.save();
		Skins.save();
		//	TODO save individual skins
		
		
		Cache.save("sata/cache.data");
		
	}
}

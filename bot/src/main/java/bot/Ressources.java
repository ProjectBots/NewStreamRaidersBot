package bot;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.RandomAccessFile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import otherlib.JP;

public class Ressources {

	public static JsonObject readFile(String path) throws FileNotFoundException {
		return JP.parseObj(new FileReader(path));
	}
}

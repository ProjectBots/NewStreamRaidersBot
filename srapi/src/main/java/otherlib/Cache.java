package otherlib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public class Cache {

	private static HashMap<String, Object> cache = new HashMap<>();

	/**
	 * stores a key value pair
	 * 
	 * @param cacheKey
	 * @param value
	 */
	public static void put(String cacheKey, Object value) {
		if(!(value instanceof Serializable))
			throw new IllegalArgumentException("Object cannot be serialized");
		
		cache.put(cacheKey, value);
	}

	/**
	 * returns a previously stored value with its key
	 * 
	 * @param <T>
	 * @param cacheKey
	 * @return a value or null
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(String cacheKey) {
		return (T) cache.get(cacheKey);
	}

	/**
	 * removes the specified value
	 * 
	 * @param cacheKey
	 * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}.)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T remove(String cacheKey) {
		return (T) cache.remove(cacheKey);
	}

	/**
	 * removes all key value pairs
	 */
	public static void clear() {
		cache.clear();
	}

	/**
	 * saves the cache to the disk
	 * @param path
	 * @throws IOException
	 */
	public static void save(String path) throws IOException {
		File file = new File(path);
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(cache);
		oos.flush();
		oos.close();
		fos.close();
	}

	/**
	 * loads the cache from the disk, overriding the current instance. <br>
	 * everything previously in the cache will be lost during this process<br>
	 * <br>
	 * if the path does not lead to a file, the cache will be empty
	 * @param path
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @return true if a file has been read, false otherwise
	 */
	@SuppressWarnings("unchecked")
	public static boolean load(String path) throws IOException, ClassNotFoundException {
		File file = new File(path);
		if(!file.isFile()) {
			cache = new HashMap<String, Object>();
			return false;
		}
		
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);

		cache = (HashMap<String, Object>) ois.readObject();

		ois.close();
		fis.close();
		
		return true;
	}
}

package test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import api.skins.Skin;
import otherlib.Cache;

public class CacheTest {

	/*
	 * Make sure before running, that the working directory is set to the top folder
	 */
	
	
	@Test
	public void testString() {
		Cache.put("a", "a");
		
		saveAndLoad();
		
		assertEquals("a", Cache.get("a"));
		
	}
	
	@Test
	public void testArray() {
		String[] arr = "abcdefg".split("");
		Cache.put("test", arr);
		
		saveAndLoad();
		
		assertArrayEquals(arr, Cache.get("test"));
	}
	
	@Test
	public void testHashSet() {
		HashSet<String> set = new HashSet<>();
		set.addAll(List.of("abcdefg".split("")));
		
		Cache.put("set", set);
		
		saveAndLoad();
		
		assertTrue(set.equals(Cache.get("set")));
	}
	
	@Test
	public void testSkin() {
		Skin skin = SkinTests.getTestSkin();
		
		Cache.put("skin", skin);
		
		saveAndLoad();
		
		assertEquals(skin, Cache.get("skin"));
	}
	
	@Test
	public void testHashMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("a", "b");
		map.put("c", "d");
		
		Cache.put("map", map);
		
		saveAndLoad();
		
		assertTrue(map.equals(Cache.get("map")));
	}
	
	
	public void saveAndLoad() {
		try {
			Cache.save("data/temp/test.json");		
			Cache.clear();	//	just to be safe
			Cache.load("data/temp/test.json");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@After
	public void clean() {
		new File("data/temp/test.json").delete();
	}
}

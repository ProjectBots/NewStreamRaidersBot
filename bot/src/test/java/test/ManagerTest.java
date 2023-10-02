package test;

import java.io.IOException;

import org.junit.Test;

import bot.Manager;

public class ManagerTest {

	
	@Test
	public void startupTest() {
		new Manager();
		
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
}

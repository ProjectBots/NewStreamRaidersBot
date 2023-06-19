package test;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

import api.profiles.RequestHelper;
import otherlib.JP;

public class requestTests {

	private static final String localhost = "http://localhost:8080/TestServer/test";
	
	private RequestHelper rh;
	
	@Before
	public void createRequestHelper() {
		rh = new RequestHelper("someCookies", "someUserAgent", "someGameDataVersion", "someClientVersion").url(localhost);
		try {
			rh.ini(e -> e.printStackTrace());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("failed to create request helper");
		}
	}
	
	@Test
	public void sendExampleToLocalhost() {
		
		JsonObject ret = rh.unlockUnit("archer", false, null, null);
		
		System.out.println(JP.prettyJson(ret));
	}
	
}

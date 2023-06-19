package api.profiles;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import api.SC;
import api.Time;
import otherlib.JP;
import otherlib.Options;

public class RequestHelper {

	{
		System.setProperty("jdk.httpclient.allowRestrictedHeaders", "Connection,Host");
	}

	private final AtomicInteger tcounter = new AtomicInteger(0);
	private final int MAX_RESEND_COUNT = 2;

	private HttpClient client;

	private String url = "https://www.streamraiders.com/api/game/?cn=";

	private final String cookies;
	private final String userAgent;

	private String userId = null;
	private String gameDataVersion;
	private String clientVersion;

	private boolean isCaptain;
	private boolean canCaptain;

	public boolean isCaptain() {
		return isCaptain;
	}

	public boolean canCaptain() {
		return canCaptain;
	}

	/**
	 * creates a new RequestHelper instance<br>
	 * used to send requests with ease
	 * 
	 * @param cookies
	 * @param userAgent
	 * @param gameDataVersion
	 * @param clientVersion
	 */
	public RequestHelper(String cookies, String userAgent, String gameDataVersion, String clientVersion) {
		client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();

		this.cookies = cookies;
		this.userAgent = userAgent;

		this.clientVersion = clientVersion;
		this.gameDataVersion = gameDataVersion;

	}

	/**
	 * changes the default url
	 * 
	 * @param url the new url
	 * @return this instance
	 */
	public RequestHelper url(String url) {
		this.url = url + "?cn=";
		return this;
	}

	/**
	 * initializes this instance
	 * 
	 * @return this instance
	 * @throws InterruptedException
	 */
	public RequestHelper ini(Consumer<Throwable> onError) throws InterruptedException {

		send("getLocalization", true, null, null, "language", "en-us");
		send("getPurchaseResults", true, null, null);

		send("getUser", true, raw -> {
			JsonObject data = raw.getAsJsonObject("data");
			userId = data.get("userId").getAsString();
			isCaptain = data.get("isCaptain").getAsInt() == 1;
			canCaptain = data.get("hasCaptainPrivileges").getAsBoolean();
		}, onError, "skipDataCheck", "true", "isLogin", "true");

		waitForFinish();

		return this;
	}

	/**
	 * function to update some parameters after a data path update
	 */
	private void reload() {
		// TODO
	}

	/**
	 * waits until all requests finished<br>
	 * this will not block new requests from being created and send, but will wait
	 * for those to finish too
	 * 
	 * @throws InterruptedException
	 */
	public void waitForFinish() throws InterruptedException {
		while (tcounter.get() > 0) {
			Thread.sleep(100);
		}
	}

	private JsonObject send(String cn, boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError,
			String... params) {
		return send(0, cn, async, onData, onError, params);
	}

	private JsonObject send(final int depth, String cn, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError, String... params) {
		if (params.length % 2 != 0)
			throw new IllegalArgumentException("parameters always come in pairs!");

		try {
			// another thread may do a update
			while (Options.isUpdatingDataPath()) {
				Thread.sleep(100);
			}

			CompletableFuture<HttpResponse<String>> raw = client.sendAsync(getPost(cn, params),
					BodyHandlers.ofString());

			if (async) {
				tcounter.getAndIncrement();
				raw.whenComplete((resp, t) -> {
					if (t != null) {
						onError.accept(t);
						return;
					}

					try {
						String out = resp.body();
						// TODO logging
						JsonObject obj = JP.parseObj(out);

						CheckReturnValue val = checkRawResponse(obj);
						switch (val) {
						case OK:
							onData.accept(obj);
							return;
						case RESEND:
							if (depth >= MAX_RESEND_COUNT)
								throw new Exception("Exceeded max_resend_count");
							send(depth + 1, cn, true, onData, onError, params);
							return;
						default:
							throw new IllegalArgumentException("unsupported return value from check: " + val);
						}
					} catch (Exception e) {
						onError.accept(e);
					}
					tcounter.getAndDecrement();
				});
				return null;
			}

			String out = raw.get().body();
			// TODO logging
			JsonObject obj = JP.parseObj(out);

			CheckReturnValue val = checkRawResponse(obj);
			switch (val) {
			case OK:
				return obj;
			case RESEND:
				if (depth >= MAX_RESEND_COUNT)
					throw new Exception("Exceeded max_resend_count");
				return send(depth + 1, cn, false, onData, onError, params);
			default:
				throw new IllegalArgumentException("unsupported return value from check: " + val);
			}
		} catch (Exception e) {
			// TODO do better
			throw new RuntimeException("sth went wrong", e);
		}
	}

	/**
	 * checks the raw response data for generic issues and tries to solve them
	 * 
	 * @param raw
	 * @return true if generic issue occured
	 */
	private CheckReturnValue checkRawResponse(JsonObject jo) {

		Time.updateSecsOff(jo.getAsJsonObject("info").get("serverTime").getAsString());
		JsonElement je = jo.get(SC.errorMessage);
		if (!je.isJsonPrimitive())
			return CheckReturnValue.OK;
		String err = je.getAsString();
		switch (err) {
		case "Account type mismatch.":
			// TODO handle better
		case "Game data mismatch.":
		case "Client lower.":
			String url = jo.getAsJsonObject("info").get("dataPath").getAsString();
			Options.update(url, JP.parseObj(getData(url)).getAsJsonObject("sheets"));
			reload();
			return CheckReturnValue.RESEND;
		}
		throw new RuntimeException("SR responded with unhandled error: " + err);
	}

	private static enum CheckReturnValue {
		OK, RESEND;
	}

	/**
	 * prepares and sends a request with the given parameters<br>
	 * if async this method will not block execution, get the data/error with the
	 * consumers
	 * 
	 * @param cn
	 * @param async
	 * @param onData  only used for async, data is ready
	 * @param onError only used for async, error occured
	 * @param params
	 * @return the data, or null if async
	 */
	/*
	 * private JsonObject send(String cn, boolean async, Consumer<JsonObject>
	 * onData, Consumer<Exception> onError, String... params) { if (async) {
	 * tcounter.getAndIncrement(); new Thread(() -> { try { JsonObject raw =
	 * prepareAndSend(cn, params); if (onData != null) onData.accept(raw); } catch
	 * (Exception e) { if (onError != null) onError.accept(e); }
	 * tcounter.getAndDecrement(); }).start(); return null; }
	 * 
	 * return prepareAndSend(cn, params); }
	 */

	/**
	 * prepares and sends a request with the given parameters
	 * 
	 * @param cn
	 * @param params
	 * @return the resulting data
	 */
	/*
	 * private JsonObject prepareAndSend(String cn, String... params) { if
	 * (params.length % 2 != 0) throw new
	 * IllegalArgumentException("parameters always come in pairs!");
	 * 
	 * JsonObject raw = actuallySend(params, cn); if (checkRawResponse(raw)) raw =
	 * actuallySend(params, cn);
	 * 
	 * return raw; }
	 */

	/**
	 * creates and sends a request with the given parameters
	 * 
	 * @param params
	 * @param cn
	 * @return
	 */
	/*
	 * private JsonObject actuallySend(String[] params, String cn) { while
	 * (isUpdatingDataPath) { try { Thread.sleep(100); } catch (InterruptedException
	 * e) { e.printStackTrace(); } }
	 * 
	 * try { String out = client.sendAsync(getPost(cn, params),
	 * BodyHandlers.ofString()).get().body(); // TODO logging return
	 * JP.parseObj(out); } catch (InterruptedException | ExecutionException e) { //
	 * TODO do better throw new RuntimeException("sth went wrong", e); } }
	 */

	/**
	 * creates a get request for a specific url with all headers set
	 * 
	 * @param params
	 * @param cn
	 * @return resulting request
	 */
	private HttpRequest getGet(String url) {
		return HttpRequest.newBuilder().uri(URI.create(url)).version(Version.HTTP_1_1).timeout(Duration.ofSeconds(30))
				.header("Accept", "*/*").header("Accept-Encoding", "gzip, deflate, br")
				.header("Accept-Language", "en-US,en;q=0.5").header("Connection", "keep-alive")
				.header("Host", "www.streamraiders.com").header("Origin", "https://www.streamraiders.com")
				.header("Referer", "https://www.streamraiders.com/").header("Sec-Fetch-Dest", "empty")
				.header("Sec-Fetch-Mode", "cors").header("Sec-Fetch-Site", "same-origin")
				.header("User-Agent", userAgent).GET().build();
	}

	// TODO check if still relevant
	private static final Set<String> mapPaths = Collections.unmodifiableSet(new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("https://d1vngzyege2qd5.cloudfront.net/prod1/");
			add("https://d2k2g0zg1te1mr.cloudfront.net/maps/");
		}
	});

	public JsonObject getMapData(String map) {
		String ret;
		for (String path : mapPaths) {
			ret = getMapData(map, path);
			if (ret != null && !ret.contains("AccessDenied"))
				return JP.parseObj(ret);
		}
		return null;
	}

	private String getMapData(String map, String path) {
		try {
			return client.sendAsync(getGet(path + map + ".txt"), BodyHandlers.ofString()).get().body();
		} catch (InterruptedException | ExecutionException e) {
			// TODO do better
			e.printStackTrace();
			return null;
		}
	}

	private String getData(String dataPath) {
		try {
			return client.sendAsync(getGet(dataPath), BodyHandlers.ofString()).get().body();
		} catch (InterruptedException | ExecutionException e) {
			// TODO do better
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * creates a post request with the given parameters and all headers set
	 * 
	 * @param params
	 * @param cn
	 * @return resulting request
	 */
	private HttpRequest getPost(String cn, String[] params) {
		return HttpRequest.newBuilder().uri(URI.create(url + cn)).version(Version.HTTP_1_1)
				.timeout(Duration.ofSeconds(30)).header("Accept", "*/*").header("Accept-Encoding", "gzip, deflate, br")
				.header("Accept-Language", "en-US,en;q=0.5").header("Connection", "keep-alive")
				.header("Content-Type", "application/x-www-form-urlencoded").header("Cookie", cookies)
				.header("Host", "www.streamraiders.com").header("Origin", "https://www.streamraiders.com")
				.header("Protocol", clientVersion).header("Referer", "https://www.streamraiders.com/game/")
				.header("Sec-Fetch-Dest", "empty").header("Sec-Fetch-Mode", "cors")
				.header("Sec-Fetch-Site", "same-origin").header("User-Agent", userAgent)
				.POST(getUrlEncodedBody(getParamsFull(cn, params))).build();
	}

	/**
	 * Translates the given parameters into application/x-www-form-urlencodedformat
	 * and creates the request body with the result
	 * 
	 * @param params
	 * @return a request body in the application/x-www-form-urlencodedformat
	 */
	private BodyPublisher getUrlEncodedBody(String[] params) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < params.length; i += 2) {
			sb.append(params[i]).append("=").append(URLEncoder.encode(params[i + 1], StandardCharsets.UTF_8))
					.append("&");
		}
		if (params.length > 0)
			sb.delete(sb.length() - 1, sb.length());
		return HttpRequest.BodyPublishers.ofString(sb.toString());
	}

	/**
	 * creates an ordered array containing all parameters needed for the request
	 * 
	 * @param cn
	 * @param add specific parameters to add
	 * @return the resulting array
	 */
	private String[] getParamsFull(String cn, String... add) {

		int i = ((userId != null ? 2 : 0) + 4) * 2 + add.length;

		String[] ret = new String[i];

		i = 0;

		if (userId != null) {
			ret[i++] = "userId";
			ret[i++] = cn.equals("getUserEventProgression") ? "" : userId;
			ret[i++] = "isCaptain";
			ret[i++] = isCaptain ? "1" : "0";
		}

		ret[i++] = "gameDataVersion";
		ret[i++] = gameDataVersion;
		ret[i++] = "command";
		ret[i++] = cn;

		System.arraycopy(add, 0, ret, i, add.length);
		i += add.length;

		ret[i++] = "clientVersion";
		ret[i++] = clientVersion;
		ret[i++] = "clientPlatform";
		ret[i++] = "WebGL";

		return ret;
	}

	public JsonObject getUser(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("getUser", async, onData, onError);
	}

	public JsonObject switchUserAccountType(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("switchUserAccountType", async, onData, onError);
	}

	public JsonObject getCurrentTime(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("getCurrentTime", async, onData, onError);
	}

	public JsonObject unlockUnit(String unitType, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("unlockUnit", async, onData, onError, "unitType", unitType);
	}

	public JsonObject upgradeUnit(String unitType, String unitLevel, String unitId, boolean async,
			Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("upgradeUnit", async, onData, onError, "unitType", unitType, "unitLevel", unitLevel, "unitId",
				unitId);
	}

	public JsonObject specializeUnit(String unitType, String unitId, String specializationUid, boolean async,
			Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("specializeUnit", async, onData, onError, "unitId", unitId, "unitType", unitType, "unitLevel", "19",
				"specializationUid", specializationUid);
	}

	public JsonObject respecializeUnit(String unitId, String specializationUid, boolean async,
			Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("unlockUnit", async, onData, onError, "unitId", unitId, "specializationUid", specializationUid);
	}

	public JsonObject getAvailableCurrencies(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("getAvailableCurrencies", async, onData, onError);
	}

	// TODO
	public JsonObject collectQuestReward(String slotId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("collectQuestReward", async, onData, onError, "slotId", slotId);
	}

	public JsonObject getUserQuests(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("getUserQuests", async, onData, onError);
	}

	public JsonObject getCurrentStoreItems(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("getCurrentStoreItems", async, onData, onError);
	}

	public JsonObject purchaseStoreItem(String itemId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("purchaseStoreItem", async, onData, onError, "itemId", itemId);
	}

	public JsonObject grantDailyDrop(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("grantDailyDrop", async, onData, onError, "storeUid", "dailydrop");
	}

	public JsonObject purchaseChestItem(String itemId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("purchaseChestItem", async, onData, onError, "itemId", itemId);
	}

	public JsonObject purchaseStoreSkin(String itemId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("purchaseStoreSkin", async, onData, onError, "itemId", itemId);
	}

	public JsonObject purchaseStoreRefresh(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("purchaseStoreRefresh", async, onData, onError);
	}

	public JsonObject equipSkin(String unitId, String skinUid, boolean equip, boolean async,
			Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("equipSkin", async, onData, onError, "unitId", unitId, "skinUid", skinUid, "isEquipped",
				booAsNumString(equip));
	}

	// TODO
	public JsonObject grantEventReward(String eventId, String rewardTier, boolean collectBattlePass, boolean async,
			Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("grantEventReward", async, onData, onError, "eventId", eventId, "rewardTier", rewardTier,
				"collectBattlePass", booAsString(collectBattlePass));
	}

	public JsonObject getUserEventProgression(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		// userId will be set to "", see getParamsFull
		return send("getUserEventProgression", async, onData, onError);
	}

	public JsonObject getCaptainsForSearch(String page, String resultsPerPage, String seed, boolean fav, boolean live,
			Boolean roomCodes, String mode, boolean searchForCaptain, String name, boolean async,
			Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		JsonObject filter = new JsonObject();
		filter.addProperty("ambassadors", "false");
		if (fav)
			filter.addProperty("favorite", "true");
		if (name != null)
			filter.addProperty((searchForCaptain ? "twitchUserName" : "mainGame"), name);
		if (live)
			filter.addProperty("isLive", "1");
		if (roomCodes != null)
			filter.addProperty("roomCodes", "" + roomCodes);
		if (mode != null)
			filter.addProperty("mode", mode);

		return send("getCaptainsForSearch", async, onData, onError, "page", page, "resultsPerPage", resultsPerPage,
				"filters", filter.toString(), "seed", seed == null ? "0" : seed);
	}

	public JsonObject updateFavoriteCaptains(boolean fav, String captainId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("updateFavoriteCaptains", async, onData, onError, "isFavorited", booAsString(fav), "captainId",
				captainId);
	}

	public JsonObject addPlayerToRaid(String userSortIndex, String captainId, boolean async,
			Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("addPlayerToRaid", async, onData, onError, "userSortIndex", userSortIndex, "captainId", captainId);
	}

	public JsonObject leaveCaptain(String captainId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("leaveCaptain", async, onData, onError, "captainId", captainId);
	}

	public JsonObject getActiveRaidsByUser(int[] placementStartIndices, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		if (placementStartIndices.length % 2 != 0)
			throw new IllegalArgumentException("placementStartIndices comes in pairs");
		JsonObject psi = new JsonObject();
		for (int i = 0; i < placementStartIndices.length; i += 2)
			psi.addProperty("" + placementStartIndices[i], placementStartIndices[i + 1]);
		return send("getActiveRaidsByUser", async, onData, onError, "placementStartIndices", psi.toString());
	}

	public JsonObject getUserDungeonInfoForRaid(String raidId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("getUserDungeonInfoForRaid", async, onData, onError, "raidId", raidId);
	}

	public JsonObject getRaid(String raidId, String placementStartIndex, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("getRaid", async, onData, onError, "raidId", raidId, "maybeSendNotifs", "False",
				placementStartIndex);
	}

	public JsonObject getRaidPlan(String raidId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("getRaidPlan", async, onData, onError, "raidId", raidId);
	}

	public JsonObject addToRaid(String raidId, String placementData, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("addToRaid", async, onData, onError, "raidId", raidId, "placementData", placementData);
	}

	public JsonObject getRaidStatsByUser(String raidId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("getRaidStatsByUser", async, onData, onError, "raidId", raidId);
	}

	public JsonObject getUserUnits(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("getUserUnits", async, onData, onError);
	}

	public JsonObject getUserItems(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("getUserItems", async, onData, onError);
	}

	public JsonObject getUserSouls(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("getUserSouls", async, onData, onError);
	}

	public JsonObject extractSoul(String unitId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("extractSoul", async, onData, onError, "unitId", unitId);
	}

	public JsonObject equipSoul(String soulId, String unitId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("equipSoul", async, onData, onError, "soulId", soulId, "unitId", unitId);
	}

	public JsonObject unequipSoul(String soulId, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("unequipSoul", async, onData, onError, "soulId", soulId);
	}

	public JsonObject grantTeamReward(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("grantTeamReward", async, onData, onError);
	}

	public JsonObject grantEventQuestMilestoneReward(boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("grantEventQuestMilestoneReward", async, onData, onError);
	}

	public JsonObject getActiveAmbassadors(boolean async, Consumer<JsonObject> onData, Consumer<Throwable> onError) {
		return send("getActiveAmbassadors", async, onData, onError);
	}

	public JsonObject getOpenCountTrackedChests(boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("getOpenCountTrackedChests", async, onData, onError);
	}

	public JsonObject redeemProductCode(String code, boolean async, Consumer<JsonObject> onData,
			Consumer<Throwable> onError) {
		return send("redeemProductCode", async, onData, onError, "actionSource", "store_cta", "code", code);
	}

	/**
	 * SR sometimes uses "True" and "False"
	 * 
	 * @param b
	 * @return "True" or "False"
	 */
	private String booAsString(boolean b) {
		return b ? "True" : "False";
	}

	/**
	 * SR sometimes uses "0" and "1"
	 * 
	 * @param b
	 * @return "0" (false) or "1" (true)
	 */
	private String booAsNumString(boolean b) {
		return b ? "0" : "1";
	}
}

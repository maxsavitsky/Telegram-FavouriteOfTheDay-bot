package com.maxsavitsky.favouriteoftheday;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UserInfoRetriever {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoRetriever.class);

	private static final HttpClient httpClient = HttpClient.newBuilder().build();

	public static UserInfo retrieve(long userId, long chatId) throws IOException, InterruptedException {
		return retrieve(userId, chatId, false);
	}

	public static UserInfo retrieve(long userId, long chatId, boolean isChatAdministratorsCalled) throws IOException, InterruptedException {
		String url = "https://api.telegram.org/bot" + TelegramBot.getToken() + "/getChatMember?chat_id=" + chatId + "&user_id=" + userId;
		LOGGER.info(url);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if(response.statusCode() != 200){
			LOGGER.error(response.body());
			if(isChatAdministratorsCalled)
				return null;
			LOGGER.info("Trying to get chat administrators and then get user info");
			return retrieveFromAdministrators(userId, chatId);
		}
		String body = response.body();
		JSONObject jsonObject = new JSONObject(body);
		JSONObject userObject = jsonObject.getJSONObject("result").getJSONObject("user");
		return fromJSON(userObject);
	}

	private static UserInfo retrieveFromAdministrators(long userId, long chatId) throws IOException, InterruptedException {
		String url = "https://api.telegram.org/bot" + TelegramBot.getToken() + "/getChatAdministrators?chat_id=" + chatId;
		LOGGER.info(url);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if(response.statusCode() != 200){
			return retrieve(userId, chatId, true);
		}
		String body = response.body();
		JSONObject jsonObject = new JSONObject(body);
		JSONArray resultArray = jsonObject.getJSONArray("result");
		for(int i = 0; i < resultArray.length(); i++){
			JSONObject userObject = resultArray.getJSONObject(i).getJSONObject("user");
			if(userObject.getLong("id") == userId)
				return fromJSON(userObject);
		}
		return retrieve(userId, chatId, true);
	}

	private static UserInfo fromJSON(JSONObject userObject){
		String firstName = userObject.getString("first_name");
		String lastName = userObject.optString("last_name");
		String username = userObject.optString("username");
		if(username != null && username.isEmpty())
			username = null;
		return new UserInfo(firstName + (lastName != null ? " " + lastName : ""), username, userObject.getLong("id"));
	}

	public record UserInfo(String name, String username, long id) {
	}

}

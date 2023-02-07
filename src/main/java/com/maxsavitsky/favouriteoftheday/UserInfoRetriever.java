package com.maxsavitsky.favouriteoftheday;

import com.maxsavitsky.favouriteoftheday.command.BaseBotCommand;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UserInfoRetriever {

	private static final Logger logger = LoggerFactory.getLogger(UserInfoRetriever.class);

	private static final HttpClient httpClient = HttpClient.newBuilder().build();

	public static UserInfo retrieve(long userId, long chatId) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://api.telegram.org/bot" + TelegramBot.getToken() + "/getChatMember?chat_id=" + chatId + "&user_id=" + userId))
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if(response.statusCode() != 200){
			logger.error(response.body());
			return null;
		}
		String body = response.body();
		JSONObject jsonObject = new JSONObject(body);
		JSONObject userObject = jsonObject.getJSONObject("result").getJSONObject("user");
		String firstName = userObject.getString("first_name");
		String lastName = userObject.optString("last_name");
		String username = userObject.optString("username");
		return new UserInfo(firstName + (lastName != null ? " " + lastName : ""), username, userId);
	}

	public record UserInfo(String name, String username, long id) {
	}

}

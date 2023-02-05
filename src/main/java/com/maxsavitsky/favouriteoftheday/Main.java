package com.maxsavitsky.favouriteoftheday;

import com.maxsavteam.ciconia.CiconiaApplication;
import com.maxsavteam.ciconia.annotation.Configuration;
import com.maxsavteam.ciconia.annotation.KeepAlive;
import com.maxsavteam.ciconia.annotation.PostInitialization;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@KeepAlive
public class Main {

	public static void main(String[] args) {
		CiconiaApplication.run(Main.class);
	}

	@PostInitialization
	public void initBot(TelegramBot telegramBot) throws TelegramApiException {
		TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
		botsApi.registerBot(telegramBot);
	}

}

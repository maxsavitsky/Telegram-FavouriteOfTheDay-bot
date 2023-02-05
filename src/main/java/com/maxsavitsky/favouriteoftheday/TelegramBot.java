package com.maxsavitsky.favouriteoftheday;

import com.maxsavitsky.favouriteoftheday.command.BaseBotCommand;
import com.maxsavteam.ciconia.annotation.Component;
import com.maxsavteam.ciconia.annotation.Implicit;
import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingCommandBot {

	public TelegramBot(@Implicit List<BaseBotCommand> commands){
		for(BaseBotCommand command : commands){
			register(command);
		}
	}


	@Override
	public String getBotUsername() {
		return "Favourite Of The Day Bot";
	}

	@Override
	public String getBotToken() {
		return getToken();
	}

	@Override
	public void processNonCommandUpdate(Update update) {

	}

	public static String getToken(){
		return System.getenv("FAVOURITE_OF_THE_DAY_BOT_TOKEN");
	}

}

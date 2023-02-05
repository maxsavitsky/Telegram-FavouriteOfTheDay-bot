package com.maxsavitsky.favouriteoftheday.command;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public abstract class BaseBotCommand implements IBotCommand {

	private final String commandId;
	private final String commandDescription;

	public BaseBotCommand(String commandId, String commandDescription) {
		this.commandId = commandId;
		this.commandDescription = commandDescription;
	}

	@Override
	public String getCommandIdentifier() {
		return commandId;
	}

	@Override
	public String getDescription() {
		return commandDescription;
	}

	public static void sendTextMessage(String text, long chatId, AbsSender absSender) throws TelegramApiException {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(chatId);
		sendMessage.setText(text);
		absSender.execute(sendMessage);
	}
}

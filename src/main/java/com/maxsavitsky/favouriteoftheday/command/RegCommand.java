package com.maxsavitsky.favouriteoftheday.command;

import com.maxsavitsky.favouriteoftheday.DatabaseManager;
import com.maxsavteam.ciconia.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class RegCommand extends BaseBotCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegCommand.class);

	private final DatabaseManager databaseManager;

	public RegCommand(DatabaseManager databaseManager) {
		super("reg@FavouriteOfTheDay_bot", "регистрируйся, если хочешь быть потенциальным любимчиком");
		this.databaseManager = databaseManager;
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		try {
			try (PreparedStatement statement = databaseManager.prepareStatement(
					"SELECT id FROM users WHERE user_id = ? AND chat_id = ?"
			)) {
				statement.setLong(1, message.getFrom().getId());
				statement.setLong(2, message.getChatId());
				if(statement.executeQuery().next()) {
					sendTextMessage("Ты уже потенциальный любимчик", message.getChatId(), absSender);
					return;
				}
			}
			try (PreparedStatement statement = databaseManager.prepareStatement(
					"INSERT INTO users (user_id, chat_id) VALUES (?, ?)"
			)) {
				statement.setLong(1, message.getFrom().getId());
				statement.setLong(2, message.getChatId());
				if(statement.executeUpdate() != 0) {
					sendTextMessage("Теперь ты потенциальный любимчик", message.getChatId(), absSender);
				}else{
					sendTextMessage("Что-то пошло не так", message.getChatId(), absSender);
				}
			}
		} catch (SQLException | TelegramApiException e) {
			LOGGER.error("Registration failed", e);
		}
	}
}

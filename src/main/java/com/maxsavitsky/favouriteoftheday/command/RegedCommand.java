package com.maxsavitsky.favouriteoftheday.command;

import com.maxsavitsky.favouriteoftheday.DatabaseManager;
import com.maxsavteam.ciconia.annotation.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class RegedCommand extends BaseBotCommand {

	private final DatabaseManager databaseManager;

	public RegedCommand(DatabaseManager databaseManager) {
		super("reged", "показать кол-во потенциальных любимчиков");
		this.databaseManager = databaseManager;
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		try {
			try (PreparedStatement statement = databaseManager.prepareStatement(
					"SELECT COUNT(*) FROM users WHERE chat_id = ?"
			)) {
				statement.setLong(1, message.getChatId());
				ResultSet resultSet = statement.executeQuery();
				if(resultSet.next()) {
					sendTextMessage("В этом чате %d потенциальных любимчиков".formatted(resultSet.getInt(1)), message.getChatId(), absSender);
				}else{
					sendTextMessage("Что-то пошло не так", message.getChatId(), absSender);
				}
			}
		} catch (SQLException | TelegramApiException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

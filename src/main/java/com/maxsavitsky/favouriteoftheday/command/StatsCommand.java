package com.maxsavitsky.favouriteoftheday.command;

import com.maxsavitsky.favouriteoftheday.DatabaseManager;
import com.maxsavitsky.favouriteoftheday.UserInfoRetriever;
import com.maxsavteam.ciconia.annotation.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class StatsCommand extends BaseBotCommand {

	private final DatabaseManager databaseManager;

	public StatsCommand(DatabaseManager databaseManager) {
		super("favouritestats@FavouriteOfTheDay_bot", "статистика любимчиков");
		this.databaseManager = databaseManager;
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		try {
			try (PreparedStatement statement = databaseManager.prepareStatement(
					"SELECT * FROM users WHERE chat_id = ? AND xuesos_count > 0 ORDER BY xuesos_count DESC"
			)) {
				statement.setLong(1, message.getChatId());
				ResultSet resultSet = statement.executeQuery();
				StringBuilder sb = new StringBuilder();
				sb.append("Наши ✨любимчики✨. Вот они, сверху вниз:\n");
				int i = 1;
				while(resultSet.next()){
					long userId = resultSet.getLong("user_id");
					UserInfoRetriever.UserInfo userInfo = UserInfoRetriever.retrieve(userId, message.getChatId());
					if(userInfo == null){
						sendTextMessage("Что-то пошло не так (error getting user " + userId + ")", message.getChatId(), absSender);
					}else{
						sb.append(i).append(") ").append(userInfo.name());
						if(userInfo.username() != null)
							sb.append(" ([@%s](tg://user?id=%d))".formatted(userInfo.username(), userId));
						sb.append(" - ").append(resultSet.getInt("xuesos_count")).append(" раз(а)");
					}
				}
				sendTextMessage(sb.toString(), message.getChatId(), absSender);
			} catch (SQLException | IOException | InterruptedException e) {
				e.printStackTrace();
				sendTextMessage("Что-то пошло не так", message.getChatId(), absSender);
			}
		}catch (TelegramApiException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

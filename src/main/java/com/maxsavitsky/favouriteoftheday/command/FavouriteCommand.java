package com.maxsavitsky.favouriteoftheday.command;

import com.maxsavitsky.favouriteoftheday.DatabaseManager;
import com.maxsavitsky.favouriteoftheday.UserInfoRetriever;
import com.maxsavteam.ciconia.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

@Component
public class FavouriteCommand extends BaseBotCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(FavouriteCommand.class);

	private final Map<Long, Boolean> currentlyRunningMap = new HashMap<>();

	private final DatabaseManager databaseManager;

	public FavouriteCommand(DatabaseManager databaseManager) {
		super("favourite@FavouriteOfTheDay_bot", "выбирает любимчика");
		this.databaseManager = databaseManager;
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		if(currentlyRunningMap.getOrDefault(message.getChatId(), false)){
			return;
		}
		try(PreparedStatement statement = databaseManager.prepareStatement("SELECT chat_id, user_id, UNIX_TIMESTAMP(timestamp) AS unix_timestamp FROM current_xuesos WHERE chat_id = ?")){
			statement.setLong(1, message.getChatId());
			ResultSet resultSet = statement.executeQuery();
			if(!resultSet.next() || isExpired(resultSet.getLong("unix_timestamp"))) {
				selectNew(absSender, message);
			}else{
				printFavourite(resultSet.getLong("user_id"), message.getChatId(), absSender);
			}
		}catch (SQLException | TelegramApiException | IOException | InterruptedException e){
			LOGGER.error("Error while processing favourite command", e);
		}
	}

	private boolean isExpired(long timestamp){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Minsk"));
		// reset to midnight
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return timestamp * 1000 < calendar.getTimeInMillis();
	}

	private void selectNew(AbsSender absSender, Message message) throws SQLException, TelegramApiException {
		currentlyRunningMap.put(message.getChatId(), true);

		LOGGER.info("Selecting favourite in chat {}", message.getChatId());

		long userId;
		double probability = new Random().nextDouble();
		if(message.getChatId() != -1001780707557L || probability < 0.8) {
			try (PreparedStatement statement = databaseManager.prepareStatement(
					"SELECT user_id FROM users WHERE chat_id = ? ORDER BY RAND() LIMIT 1"
			)) {
				statement.setLong(1, message.getChatId());
				ResultSet resultSet = statement.executeQuery();
				if (!resultSet.next()) {
					BaseBotCommand.sendTextMessage("В чате нет ни одного зарегистрированного пользователя", message.getChatId(), absSender);
					return;
				}
				userId = resultSet.getLong("user_id");
			}
		}else{
			LOGGER.info("Vita with probability {}))))", probability);
			userId = 817160881;
		}

		try(PreparedStatement statement = databaseManager.prepareStatement(
				"REPLACE INTO current_xuesos (chat_id, user_id) VALUES (?, ?)"
		)){
			statement.setLong(1, message.getChatId());
			statement.setLong(2, userId);
			statement.executeUpdate();
		}

		try(PreparedStatement statement = databaseManager.prepareStatement(
				"UPDATE users SET xuesos_count = xuesos_count + 1 WHERE chat_id = ? AND user_id = ?"
		)){
			statement.setLong(1, message.getChatId());
			statement.setLong(2, userId);
			statement.executeUpdate();
		}

		String[] messages = new String[]{
				"Выбираем сегодняшнего любимчика \uD83E\uDDD0",
				"4 - Смотрим, скинули ли домашку, когда просили \uD83E\uDD28",
				"3 - Спрашиваем у Матвиенко \uD83E\uDEE3\uD83E\uDEE1",
				"2 - Читаем мысли других (возможна погрешность) \uD83E\uDD2F",
				"1 - Любимчик выбран и его нужно \"любить\" по-особенному \uD83E\uDD2D\uD83D\uDC96"
		};

		new Thread(()->{
			for (String s : messages) {
				try {
					BaseBotCommand.sendTextMessage(s, message.getChatId(), absSender);
					Thread.sleep(2000);
				} catch (Exception e) {
					LOGGER.error("Error while sending message", e);
				}
			}
			try {
				printFavourite(userId, message.getChatId(), absSender);
			} catch (Exception e) {
				LOGGER.error("Error while printing favourite", e);
			}
			currentlyRunningMap.remove(message.getChatId());
		}).start();
	}

	public static void printFavourite(long userId, long chatId, AbsSender absSender) throws IOException, InterruptedException, TelegramApiException {
		LOGGER.info("Printing favourite for user {} in chat {}", userId, chatId);
		UserInfoRetriever.UserInfo userInfo = UserInfoRetriever.retrieve(userId, chatId);
		if(userInfo == null){
			BaseBotCommand.sendTextMessage("Не удалось получить информацию о пользователе", chatId, absSender);
			return;
		}
		String mention = StatsCommand.escapeMarkdown(userInfo.name());
		if(userInfo.username() != null){
			mention += " ([@%s](tg://user?id=%d))".formatted(userInfo.username(), userId);
		}
		String message = "\uD83E\uDD73 Официально сегодня можно ✨хуесосить✨ вот этого человека - %s".formatted(mention);
		BaseBotCommand.sendTextMessage(message, chatId, absSender);
	}

}

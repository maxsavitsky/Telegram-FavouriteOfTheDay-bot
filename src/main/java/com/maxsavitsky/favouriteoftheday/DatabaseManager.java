package com.maxsavitsky.favouriteoftheday;

import com.maxsavteam.ciconia.annotation.Component;
import com.maxsavteam.ciconia.annotation.KeepAlive;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
@KeepAlive
public class DatabaseManager {

	private final String user;
	private final String pass;
	private final String dbName;
	private final String host;

	private Connection connection;

	public DatabaseManager() throws SQLException {
		user = System.getenv("DB_USER");
		pass = System.getenv("DB_PASS");
		dbName = System.getenv("DB_NAME");
		host = System.getenv("DB_HOST");

		connection = createConnection();
	}

	private Connection createConnection() throws SQLException {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser(user);
		dataSource.setPassword(pass);
		dataSource.setDatabaseName(dbName);
		dataSource.setServerName(host);
		return dataSource.getConnection();
	}

	public Connection getConnection() throws SQLException {
		if(!connection.isValid(0))
			return connection = createConnection();
		return connection;
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return getConnection().prepareStatement(sql);
	}

}

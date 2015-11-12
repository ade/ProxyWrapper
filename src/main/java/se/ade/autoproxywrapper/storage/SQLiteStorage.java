package se.ade.autoproxywrapper.storage;

import java.nio.file.*;
import java.sql.*;

import se.ade.autoproxywrapper.config.UserConfigurationPathUtil;
import se.ade.autoproxywrapper.events.*;

public abstract class SQLiteStorage {

	public static final String CREATE_BYTES_SENT_TABLE = "create table bytes_sent (stat_date date not null primary key, amount bigint not null)";
	public static final String CREATE_BYTES_RECEIVED_TABLE = "create table bytes_received (stat_date date not null primary key, amount bigint not null)";

	protected Connection connection;

	protected SQLiteStorage() {
		try{
			Class.forName("org.sqlite.JDBC");

			Path configFilePath = UserConfigurationPathUtil.getConfigFilePath("stats");
			boolean fileExists = Files.exists(configFilePath);

			connection = DriverManager.getConnection("jdbc:sqlite:" + configFilePath.toString());
			if(!fileExists) {
				initiateDatabase();
			}
		} catch (SQLException | ClassNotFoundException e) {
			EventBus.get().post(GenericLogEvent.info("Could not access data on disk: " + e.getMessage()));
		}
	}

	private void initiateDatabase() {
		try (Statement statement = connection.createStatement()) {
			statement.execute(CREATE_BYTES_SENT_TABLE);
			statement.execute(CREATE_BYTES_RECEIVED_TABLE);
		} catch (SQLException ignored) {}
	}

	protected void close() {
		try {
			if(!connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

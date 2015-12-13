package se.ade.autoproxywrapper.storage;

import java.nio.file.*;
import java.sql.*;

import se.ade.autoproxywrapper.config.UserConfigurationPathUtil;
import se.ade.autoproxywrapper.events.*;

public abstract class SQLiteStorage {

	public static final String CREATE_TRAFFIC_STATAS_TABLE = "create table traffic_stats (datepart_year int not null, datepart_month int not null, datepart_day int not null, amount_sent bigint not null, amount_received bigint not null, primary key(datepart_year, datepart_month, datepart_day))";

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
			statement.execute(CREATE_TRAFFIC_STATAS_TABLE);
		} catch (SQLException ignored) {}
	}

	public void close() {
		try {
			if(!connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

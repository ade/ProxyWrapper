package se.ade.autoproxywrapper.statistics;

import java.sql.*;
import java.time.LocalDate;

import se.ade.autoproxywrapper.events.*;
import se.ade.autoproxywrapper.storage.SQLiteStorage;

public class StatisticsStorage extends SQLiteStorage {

	private static final String GET_BYTES_SENT_QUERY = "select amount from bytes_sent where stat_date = ?";
	private static final String INSERT_BYTES_SENT_QUERY = "insert or replace into bytes_sent (stat_date, amount) values (?, ?)";
	private static final String GET_BYTES_RECEIVED_QUERY = "select amount from bytes_received where stat_date = ?";
	private static final String INSERT_BYTES_RECEIVED_QUERY = "insert or replace into bytes_received (stat_date, amount) values (?, ?)";

	private StatisticsStorage() {
		super();
	}

	public static StatisticsStorage instance() {
		return new StatisticsStorage();
	}

	public void insertStatistics(Statistics statistics) {
		try {
			String dateString = LocalDate.now().toString();
			insertBytesQuery(INSERT_BYTES_SENT_QUERY, dateString, statistics.getBytesSent());
			insertBytesQuery(INSERT_BYTES_RECEIVED_QUERY, dateString, statistics.getBytesReceived());
		} catch (SQLException e) {
			EventBus.get().post(GenericLogEvent.info("Could not save data: " + e.getMessage()));
		} finally {
			super.close();
		}
	}

	private void insertBytesQuery(String query, String dateString, long amount) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, dateString);
		preparedStatement.setLong(2, amount);
		preparedStatement.execute();
		preparedStatement.close();
	}

	public Statistics getStatistics() {
		Statistics statistics = new Statistics();
		try {
			String dateString = LocalDate.now().toString();
			statistics.setBytesSent(getBytesQuery(GET_BYTES_SENT_QUERY, dateString));
			statistics.setBytesReceived(getBytesQuery(GET_BYTES_RECEIVED_QUERY, dateString));
		} catch (SQLException e) {
			EventBus.get().post(GenericLogEvent.info("Could not load data: " + e.getMessage()));
		} finally {
			super.close();
		}
		return statistics;
	}

	private long getBytesQuery(String query, String dateString) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, dateString);
		ResultSet resultSet = preparedStatement.executeQuery();
		long value = 0L;
		if(resultSet.next()) {
			value = resultSet.getLong(1);
		}
		resultSet.close();
		preparedStatement.close();
		return value;
	}
}

package se.ade.autoproxywrapper.statistics;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import se.ade.autoproxywrapper.events.*;
import se.ade.autoproxywrapper.storage.SQLiteStorage;

public class StatisticsStorage extends SQLiteStorage {

	private static final String INSERT_TRAFFIC_STATS_QUERY = "insert or replace into traffic_stats (datepart_year, datepart_month, datepart_day, amount_sent, amount_received) values (?, ?, ?, ?, ?)";
	private static final String GET_TRAFFIC_STATS_FOR_YEAR_AND_MONTH_AND_DAY_QUERY = "select amount_sent, amount_received from traffic_stats where datepart_year = ? and datepart_month = ? and datepart_day = ?";
	private static final String GET_TRAFFIC_STATS_FOR_YEAR_AND_MONTH_QUERY = "select datepart_year, datepart_month, datepart_day, amount_sent, amount_received from traffic_stats where datepart_year = ? and datepart_month = ?";
	private static final String GET_AVAILABLE_YEARS_AND_MONTHS_QUERY = "select datepart_year, datepart_month from traffic_stats group by datepart_year, datepart_month";

	private StatisticsStorage() {
		super();
	}

	public static StatisticsStorage instance() {
		return new StatisticsStorage();
	}

	public void insertStatistics(Statistics statistics) {
		try {
			LocalDate now = LocalDate.now();
			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TRAFFIC_STATS_QUERY);
			preparedStatement.setInt(1, now.getYear());
			preparedStatement.setInt(2, now.getMonthValue());
			preparedStatement.setInt(3, now.getDayOfMonth());
			preparedStatement.setLong(4, statistics.getBytesSent());
			preparedStatement.setLong(5, statistics.getBytesReceived());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			EventBus.get().post(GenericLogEvent.info("Could not save data: " + e.getMessage()));
			super.close();
		}
	}

	public Statistics getCurrentStatistics() {
		Statistics statistics = new Statistics();
		try {
			LocalDate now = LocalDate.now();
			statistics.setDate(now);
			PreparedStatement preparedStatement = connection.prepareStatement(GET_TRAFFIC_STATS_FOR_YEAR_AND_MONTH_AND_DAY_QUERY);
			preparedStatement.setInt(1, now.getYear());
			preparedStatement.setInt(2, now.getMonthValue());
			preparedStatement.setInt(3, now.getDayOfMonth());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				statistics.setBytesSent(resultSet.getLong("amount_sent"));
				statistics.setBytesReceived(resultSet.getLong("amount_received"));
			}
			resultSet.close();
			preparedStatement.close();
		} catch (SQLException e) {
			EventBus.get().post(GenericLogEvent.info("Could not load data: " + e.getMessage()));
			super.close();
		}
		return statistics;
	}

	public Collection<Statistics> getStatistics(int year, int month) {
		Collection<Statistics> statisticsCollection = new ArrayList<>();
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(GET_TRAFFIC_STATS_FOR_YEAR_AND_MONTH_QUERY);
			preparedStatement.setInt(1, year);
			preparedStatement.setInt(2, month);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Statistics statistics = new Statistics();
				LocalDate date = LocalDate.of(resultSet.getInt("datepart_year"), resultSet.getInt("datepart_month"), resultSet.getInt("datepart_day"));
				statistics.setDate(date);
				statistics.setBytesSent(resultSet.getLong("amount_sent"));
				statistics.setBytesReceived(resultSet.getLong("amount_received"));
				statisticsCollection.add(statistics);
			}
			resultSet.close();
			preparedStatement.close();
		} catch (SQLException e) {
			EventBus.get().post(GenericLogEvent.info("Could not load data: " + e.getMessage()));
			super.close();
		}
		return statisticsCollection;
	}

	public Map<Integer, Collection<Integer>> getAvailableYearMonths() {
		Map<Integer, Collection<Integer>> availableYearMonths = new HashMap<>();
		try{
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(GET_AVAILABLE_YEARS_AND_MONTHS_QUERY);
			while(resultSet.next()) {
				if(!availableYearMonths.containsKey(resultSet.getInt("datepart_year"))) {
					availableYearMonths.put(resultSet.getInt("datepart_year"), new ArrayList<>());
				}
				availableYearMonths.get(resultSet.getInt("datepart_year")).add(resultSet.getInt("datepart_month"));
			}
		}catch (SQLException e) {
			EventBus.get().post(GenericLogEvent.info("Could not load data: " + e.getMessage()));
			super.close();
		}
		return availableYearMonths;
	}
}

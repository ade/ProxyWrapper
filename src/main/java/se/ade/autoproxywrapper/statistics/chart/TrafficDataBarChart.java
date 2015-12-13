package se.ade.autoproxywrapper.statistics.chart;

import java.time.*;
import java.util.*;

import javafx.collections.*;
import javafx.scene.chart.*;
import se.ade.autoproxywrapper.ByteUnit;
import se.ade.autoproxywrapper.statistics.Statistics;

public class TrafficDataBarChart extends TrafficDataChart {

	private Chart chartNode;

	public TrafficDataBarChart(Year year, Month month, Collection<Statistics> filteredStatisticsCollection) {
		ByteUnit byteUnit = ByteUnit.getUnitForValue(filteredStatisticsCollection.stream().mapToLong(value -> value.getBytesReceived()).max().getAsLong());

		ObservableList<String> days = FXCollections.observableArrayList();
		for (int i = 1; i <= month.length(year.isLeap()); i++) {
			days.add(Integer.toString(i));
		}

		CategoryAxis na = new CategoryAxis(days);
		NumberAxis na2 = new NumberAxis();
		BarChart<String, Number> chart = new BarChart<>(na, na2);
		chart.setTitle("Traffic");

		na.setLabel("Date");
		na2.setLabel(byteUnit.getRepresentation());

		chart.getData().addAll(getChartData(filteredStatisticsCollection, byteUnit));
		this.chartNode = chart;
	}

	private Collection<XYChart.Series<String, Number>> getChartData(Collection<Statistics> filteredStatisticsCollection, ByteUnit byteUnit) {
		Collection<XYChart.Series<String, Number>> chartData = new ArrayList<>();
		XYChart.Series sent = new XYChart.Series();
		sent.setName("Sent");
		XYChart.Series received = new XYChart.Series();
		received.setName("Received");

		filteredStatisticsCollection.stream().forEach(statistics -> {
			double bytesSent = (double) statistics.getBytesSent() / byteUnit.getMinSize();
			double bytesRecevied = (double) statistics.getBytesReceived() / byteUnit.getMinSize();

			XYChart.Data sentData = new XYChart.Data("" + statistics.getDate().getDayOfMonth(), bytesSent);
			sent.getData().add(sentData);
			sentData.setNode(sentHoverNode(bytesSent, byteUnit));
			XYChart.Data receivedData = new XYChart.Data("" + statistics.getDate().getDayOfMonth(), bytesRecevied);
			received.getData().add(receivedData);
			receivedData.setNode(receivedHoverNode(bytesRecevied, byteUnit));
		});
		chartData.add(sent);
		chartData.add(received);
		return chartData;
	}

	@Override
	public Chart getChart() {
		return this.chartNode;
	}
}

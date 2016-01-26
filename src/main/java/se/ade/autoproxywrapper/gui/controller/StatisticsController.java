package se.ade.autoproxywrapper.gui.controller;

import static java.time.LocalDate.now;

import java.time.*;
import java.util.*;

import javafx.fxml.FXML;
import javafx.scene.chart.Chart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import se.ade.autoproxywrapper.statistics.*;
import se.ade.autoproxywrapper.statistics.chart.*;

public class StatisticsController {

	private static final String LINE_CHART = "Line";
	private static final String BAR_CHART = "Bar";

	@FXML
	public AnchorPane contentPane;

	@FXML
	public ChoiceBox<String> chartTypeChoiceBox;

	@FXML
	public ChoiceBox<Year> yearChoiceBox;

	@FXML
	public ChoiceBox<Month> monthChoiceBox;

	private Stage window;

	private Map<Integer, Collection<Integer>> availableYearMonths;

	@FXML
	public void initialize() {
		StatisticsStorage statisticsStorage = StatisticsStorage.instance();
		availableYearMonths = statisticsStorage.getAvailableYearMonths();
		statisticsStorage.close();

		chartTypeChoiceBox.getItems().addAll(LINE_CHART, BAR_CHART);
		chartTypeChoiceBox.getSelectionModel().select(LINE_CHART);

		setupYearAndMonthChoiceBoxes(Year.now(), true);

		yearChoiceBox.setOnAction(event -> {
			setupYearAndMonthChoiceBoxes(yearChoiceBox.getSelectionModel().getSelectedItem(), false);
			createAndAddChart();
		});
		monthChoiceBox.setOnAction(event -> createAndAddChart());
		chartTypeChoiceBox.setOnAction(event -> createAndAddChart());

		createAndAddChart();
	}

	private void setupYearAndMonthChoiceBoxes(Year selectedYear, boolean createMonthIfNotExist) {
		yearChoiceBox.getItems().clear();
		monthChoiceBox.getItems().clear();

		availableYearMonths.keySet().stream().forEach(year -> yearChoiceBox.getItems().add(Year.of(year)));

		Month selectedMonth = now().getMonth();
		if (!availableYearMonths.containsKey(selectedYear.getValue())) {
			yearChoiceBox.getItems().add(selectedYear);
			availableYearMonths.put(selectedYear.getValue(), new ArrayList<>());
		}
		yearChoiceBox.getSelectionModel().select(selectedYear);

		Collection<Integer> months = availableYearMonths.get(selectedYear.getValue());
		months.stream().forEach(month -> monthChoiceBox.getItems().add(Month.of(month)));

		if (months.stream().noneMatch(month -> selectedMonth.getValue() == month) && createMonthIfNotExist) {
			monthChoiceBox.getItems().add(selectedMonth);
			monthChoiceBox.getSelectionModel().select(selectedMonth);
		} else {
			monthChoiceBox.getSelectionModel().selectFirst();
		}
	}

	private void createAndAddChart() {
		Year year = yearChoiceBox.getSelectionModel().getSelectedItem();
		Month month = monthChoiceBox.getSelectionModel().getSelectedItem();
		if (year == null || month == null) {
			return;
		}

		StatisticsStorage statisticsStorage = StatisticsStorage.instance();
		Collection<Statistics> filteredStatisticsCollection = statisticsStorage.getStatistics(year.getValue(), month.getValue());
		statisticsStorage.close();

		Chart chart;
		switch (chartTypeChoiceBox.getSelectionModel().getSelectedItem()) {
			case BAR_CHART:
				chart = new TrafficDataBarChart(year, month, filteredStatisticsCollection).getChart();
				break;
			case LINE_CHART:
			default:
				chart = new TrafficDataLineChart(year, month, filteredStatisticsCollection).getChart();
		}
		contentPane.setBottomAnchor(chart, 0.0);
		contentPane.setTopAnchor(chart, 0.0);
		contentPane.setLeftAnchor(chart, 0.0);
		contentPane.setRightAnchor(chart, 0.0);
		contentPane.getChildren().clear();
		contentPane.getChildren().add(chart);
	}

	@FXML
	public void cancel() {
		window.close();
	}

	public void setWindow(Stage window) {
		this.window = window;
	}
}

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
		chartTypeChoiceBox.getItems().addAll(LINE_CHART, BAR_CHART);
		chartTypeChoiceBox.getSelectionModel().select(LINE_CHART);
		chartTypeChoiceBox.setOnAction(event -> createAndAddChart(yearChoiceBox.getSelectionModel().getSelectedItem(), monthChoiceBox.getSelectionModel().getSelectedItem()));

		setupYearAndMonthChoiceBoxes();

		createAndAddChart(Year.now(), now().getMonth());
	}

	private void setupYearAndMonthChoiceBoxes() {
		StatisticsStorage statisticsStorage = StatisticsStorage.instance();
		availableYearMonths = statisticsStorage.getAvailableYearMonths();
		statisticsStorage.close();

		availableYearMonths.keySet().stream().forEach(year -> yearChoiceBox.getItems().add(Year.of(year)));

		Year yearString = Year.now();
		Month monthString = now().getMonth();
		if (!availableYearMonths.containsKey(now().getYear())) {
			yearChoiceBox.getItems().add(yearString);
			monthChoiceBox.getItems().add(monthString);
			yearChoiceBox.getSelectionModel().select(yearString);
			monthChoiceBox.getSelectionModel().select(monthString);
		} else {
			yearChoiceBox.getSelectionModel().select(yearString);
			Collection<Integer> monthsInYear = availableYearMonths.get(now().getYear());
			monthsInYear.stream().forEach(month -> monthChoiceBox.getItems().add(Month.of(month)));
			if (monthsInYear.stream().noneMatch(month -> now().getMonthValue() == month)) {
				monthChoiceBox.getItems().add(monthString);
			}
			monthChoiceBox.getSelectionModel().select(monthString);
		}

		yearChoiceBox.setOnAction(event -> createAndAddChart(yearChoiceBox.getSelectionModel().getSelectedItem(), monthChoiceBox.getSelectionModel().getSelectedItem()));
		monthChoiceBox.setOnAction(event -> createAndAddChart(yearChoiceBox.getSelectionModel().getSelectedItem(), monthChoiceBox.getSelectionModel().getSelectedItem()));
	}

	private void createAndAddChart(Year year, Month month) {
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

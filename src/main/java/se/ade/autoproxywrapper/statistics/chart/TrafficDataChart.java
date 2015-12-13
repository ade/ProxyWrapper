package se.ade.autoproxywrapper.statistics.chart;

import java.text.DecimalFormat;

import javafx.scene.chart.Chart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import se.ade.autoproxywrapper.ByteUnit;

public abstract class TrafficDataChart {

	private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

	public abstract Chart getChart();

	protected StackPane sentHoverNode(double value, ByteUnit byteUnit) {
		return hoverNode(0, value, byteUnit);
	}

	protected StackPane receivedHoverNode(double value, ByteUnit byteUnit) {
		return hoverNode(1, value, byteUnit);
	}

	private StackPane hoverNode(int index, double value, ByteUnit byteUnit) {
		StackPane pane = new StackPane();
		pane.setPrefSize(7, 7);
		pane.setPickOnBounds(false);
		Label label = new Label(decimalFormat.format(value) + " " + byteUnit.getRepresentation());
		label.getStyleClass().addAll("default-color" + index, "chart-line-symbol", "chart-series-line");
		label.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
		label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
		pane.setOnMouseEntered(event -> {
			pane.getChildren().setAll(label);
			pane.toFront();
		});
		pane.setOnMouseExited(event -> {
			pane.getChildren().clear();
		});
		return pane;
	}
}

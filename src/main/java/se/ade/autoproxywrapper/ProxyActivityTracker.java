package se.ade.autoproxywrapper;

import org.littleshoot.proxy.*;
import se.ade.autoproxywrapper.statistics.*;

public class ProxyActivityTracker extends ActivityTrackerAdapter {

	private Statistics statistics;

	public ProxyActivityTracker() {
		StatisticsStorage statisticsStorage = StatisticsStorage.instance();
		statistics = statisticsStorage.getCurrentStatistics();
		statisticsStorage.close();
	}

	public Statistics getStatistics() {
		return statistics;
	}

	@Override
	public void bytesReceivedFromClient(FlowContext flowContext, int numberOfBytes) {
		statistics.addBytesSent(numberOfBytes);
	}

	@Override
	public void bytesSentToClient(FlowContext flowContext, int numberOfBytes) {
		statistics.addBytesReceived(numberOfBytes);
	}
}

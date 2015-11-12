package se.ade.autoproxywrapper.statistics;

import se.ade.autoproxywrapper.MiniHttpProxy;
import se.ade.autoproxywrapper.events.*;

public class StatisticsTask implements Runnable{

	private MiniHttpProxy miniHttpProxy;

	public StatisticsTask(MiniHttpProxy miniHttpProxy) {
		this.miniHttpProxy = miniHttpProxy;
	}

	@Override
	public void run() {
		StatisticsStorage.instance().insertStatistics(miniHttpProxy.getStatistics());
		EventBus.get().post(GenericLogEvent.verbose("Saved statistics data"));
	}
}

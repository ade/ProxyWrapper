package se.ade.autoproxywrapper.loopback;

import com.google.common.eventbus.Subscribe;
import se.ade.autoproxywrapper.ModeSelector;
import se.ade.autoproxywrapper.ProxyMode;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.GenericLogEvent;

import java.util.ArrayList;

/**
 * Created by adrnil on 25/05/16.
 */
public class LoopBackTestApp {
	public static Object eventListener = new Object() {
		@Subscribe
		public void onEvent(GenericLogEvent e) {
			System.out.println(e.type.name() + ": " + e.message);
		}
	};

	public static void main(String args[]) {
		ArrayList<LoopBackConfig> configs = new ArrayList<>();
		configs.add(new LoopBackConfig("www.google.com", 80, 4545, "google"));
		LoopBackService service = new LoopBackService(new ModeSelector() {
			@Override
			public ProxyMode getMode() {
				return ProxyMode.USE_PROXY;
			}
		}, configs);
		EventBus.get().register(eventListener);
		service.start();

		while (true) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				//
			}
		}
	}
}

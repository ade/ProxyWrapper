package se.ade.autoproxywrapper.loopback;

import com.google.common.eventbus.Subscribe;
import naga.NIOServerSocket;
import naga.NIOService;
import se.ade.autoproxywrapper.MiniHttpProxy;
import se.ade.autoproxywrapper.ModeSelector;
import se.ade.autoproxywrapper.events.DetectModeEvent;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.GenericLogEvent;
import se.ade.autoproxywrapper.events.ModeChangedEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adrnil on 25/05/16.
 */
public class LoopBackService extends Thread {
	NIOService nioService;
	private List<LoopBackConfig> configs;
	private HashMap<LoopBackConfig, LoopBackServer> serverSockets;
	boolean running = true;
	ModeSelector modeSelector;

	private Object eventListener = new Object() {
		@Subscribe
		public void onEvent(ModeChangedEvent e) {

		}
	};

	public LoopBackService(ModeSelector modeSelector, List<LoopBackConfig> configs) {
		this.configs = configs;
		this.modeSelector = modeSelector;
		try {
			this.nioService = new NIOService();
		} catch (IOException e) {
			EventBus.get().post(GenericLogEvent.error("Can't start NIOService: " + e));
		}

		serverSockets = new HashMap<>();
	}

	public void destroyService() {
		for(Map.Entry<LoopBackConfig, LoopBackServer> entry : serverSockets.entrySet()) {
			entry.getValue().close();
		}
		running = false;
	}

	public NIOService getNioService() {
		return nioService;
	}

	@Override
	public void run() {
		if(configs == null) {
			return;
		}

		for(LoopBackConfig config : configs) {
			try {
				NIOServerSocket listener = this.nioService.openServerSocket(config.getLocalPort());
				LoopBackServer server = new LoopBackServer(config, listener, this);
				serverSockets.put(config, server);
				EventBus.get().post(GenericLogEvent.verbose("Loopback service listening on port " + config.getLocalPort()));
			} catch (IOException e) {
				EventBus.get().post(GenericLogEvent.error("Can't start server on port " + config.getLocalPort() + ": " + e));
			}
		}

		while (running) {
			try {
				nioService.selectBlocking();
			} catch (IOException e) {
				EventBus.get().post(GenericLogEvent.error("Error " + e));
			}
		}

		nioService.close();

		EventBus.get().post(GenericLogEvent.verbose("LoopBack service shut down"));
	}
}

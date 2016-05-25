package se.ade.autoproxywrapper.loopback;

import naga.*;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.GenericLogEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;


/**
 * Created by adrnil on 25/05/16.
 */
public class LoopBackServer implements ServerSocketObserver {
	private final NIOServerSocket listener;
	private LoopBackConfig config;
	private ArrayList<LoopBackLocalConnection> localConnections = new ArrayList<>();
	private LoopBackService service;

	public LoopBackServer(LoopBackConfig config, NIOServerSocket listener, LoopBackService service) {
		this.config = config;
		this.listener = listener;
		this.service = service;
		this.listener.listen(this);
	}

	public void close() {
		this.listener.close();
		for(LoopBackLocalConnection connection : localConnections) {
			connection.destroy();
		}
		localConnections.clear();
	}

	public LoopBackService getService() {
		return service;
	}

	public void onConnectionClosed(LoopBackLocalConnection connection) {
		EventBus.get().post(GenericLogEvent.verbose("Closed loopback connection to " + connection.getConfig().getRemoteHost()));
	}

	@Override
	public void acceptFailed(IOException exception) {
		EventBus.get().post(GenericLogEvent.verbose("Accept failed on loopback port " + config.getLocalPort()));
	}

	@Override
	public void serverSocketDied(Exception exception) {
		EventBus.get().post(GenericLogEvent.verbose("Server socket died on loopback port " + config.getLocalPort()));
	}

	@Override
	public void newConnection(NIOSocket nioSocket) {
		LoopBackLocalConnection connection = new LoopBackLocalConnection(config, nioSocket, this);
		nioSocket.listen(connection);
		localConnections.add(connection);
	}
}

package se.ade.autoproxywrapper.loopback;

import naga.NIOSocket;
import naga.SocketObserver;
import se.ade.autoproxywrapper.ProxyMode;
import se.ade.autoproxywrapper.config.Config;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.GenericLogEvent;
import se.ade.autoproxywrapper.model.ForwardProxy;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by adrnil on 25/05/16.
 */
public class LoopBackLocalConnection implements SocketObserver {
	private final LoopBackServer loopBackServer;
	private NIOSocket localSocket;
	private NIOSocket remoteSocket;
	private LoopBackRemoteConnection remoteConnection;
	private LoopBackConfig config;
	private LinkedList<byte[]> packetBuffer = new LinkedList<>();

	public LoopBackLocalConnection(LoopBackConfig config, NIOSocket localSocket, LoopBackServer loopBackServer) {
		this.localSocket = localSocket;
		this.loopBackServer = loopBackServer;
		this.config = config;

		EventBus.get().post(GenericLogEvent.verbose("Local connection established for loopback to " + config.getRemoteHost() + ":" + config.getRemotePort() + ". Connecting..."));
	}

	@Override
	public void connectionOpened(NIOSocket nioSocket) {

		try {
			if(loopBackServer.getService().modeSelector.getMode() == ProxyMode.USE_PROXY) {
				ForwardProxy proxy = Config.getConfig().getForwardProxies().get(0); //TODO Support whole chain

				this.remoteSocket = loopBackServer.getService().getNioService().openSocket(proxy.getHost(), proxy.getPort());
				remoteConnection = new LoopBackRemoteProxiedConnection(this);
			} else {
				this.remoteSocket = loopBackServer.getService().getNioService().openSocket(config.getRemoteHost(), config.getRemotePort());
				remoteConnection = new LoopBackRemoteConnection(this);
			}

			this.remoteSocket.listen(remoteConnection);
		} catch (IOException e) {
			EventBus.get().post(GenericLogEvent.error("Unable to open connection to remote: " + e));
		}
	}

	@Override
	public void connectionBroken(NIOSocket nioSocket, Exception exception) {
		this.loopBackServer.onConnectionClosed(this);
		remoteSocket.close();
	}

	@Override
	public void packetReceived(NIOSocket socket, byte[] packet) {
		if(remoteConnection.isConnected()) {
			while(!packetBuffer.isEmpty()) {
				remoteSocket.write(packetBuffer.pop());
			}
			remoteSocket.write(packet);
		} else {
			EventBus.get().post(GenericLogEvent.verbose("Buffering " + packet.length + " bytes from local..."));
			packetBuffer.add(packet);
		}
	}

	@Override
	public void packetSent(NIOSocket socket, Object tag) {

	}

	public LoopBackConfig getConfig() {
		return config;
	}

	public void onRemoteConnected() {
		while(!packetBuffer.isEmpty()) {
			remoteSocket.write(packetBuffer.pop());
		}
	}

	public void onRemotePacketReceived(NIOSocket socket, byte[] packet) {
		this.localSocket.write(packet);
	}

	public void onRemoteClosedConnection(Exception exception) {
		EventBus.get().post(GenericLogEvent.verbose("Remote closed connection." + (exception != null ? " (Exception: " + exception + ")" : "")));
		localSocket.close();
	}

	public void destroy() {
		try {
			localSocket.close();
		} catch (Exception e) {
			//
		}

		try {
			remoteSocket.close();
		} catch (Exception e) {
			//
		}
	}
}

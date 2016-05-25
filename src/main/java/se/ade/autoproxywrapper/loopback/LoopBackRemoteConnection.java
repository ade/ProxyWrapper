package se.ade.autoproxywrapper.loopback;

import naga.NIOSocket;
import naga.SocketObserver;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.GenericLogEvent;

/**
 * Created by adrnil on 25/05/16.
 */
public class LoopBackRemoteConnection implements SocketObserver {
	protected LoopBackLocalConnection localConnection;
	protected boolean connected;

	public LoopBackRemoteConnection(LoopBackLocalConnection localConnection) {
		this.localConnection = localConnection;
	}

	@Override
	public void connectionOpened(NIOSocket nioSocket) {
		EventBus.get().post(GenericLogEvent.verbose("Connection established to remote"));
		connected = true;
		localConnection.onRemoteConnected();
	}

	@Override
	public void connectionBroken(NIOSocket nioSocket, Exception exception) {
		localConnection.onRemoteClosedConnection(exception);
	}

	@Override
	public void packetReceived(NIOSocket socket, byte[] packet) {
		localConnection.onRemotePacketReceived(socket, packet);
	}

	@Override
	public void packetSent(NIOSocket socket, Object tag) {

	}

	public boolean isConnected() {
		return connected;
	}
}

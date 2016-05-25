package se.ade.autoproxywrapper.loopback;

import naga.NIOSocket;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.GenericLogEvent;

/**
 * Created by adrnil on 25/05/16.
 */
public class LoopBackRemoteProxiedConnection extends LoopBackRemoteConnection {
	boolean connectionConfirmed;

	public LoopBackRemoteProxiedConnection(LoopBackLocalConnection localConnection) {
		super(localConnection);
	}

	@Override
	public void connectionOpened(NIOSocket nioSocket) {
		LoopBackConfig config = localConnection.getConfig();
		byte[] proxyRequest = ("CONNECT " + config.getRemoteHost() + ":" + config.getRemotePort() + " HTTP/1.1\n\n").getBytes();
		nioSocket.write(proxyRequest);
	}

	@Override
	public void packetReceived(NIOSocket socket, byte[] packet) {
		if(!connectionConfirmed) {
			String s = new String(packet);
			if(s.contains("\n")) {
				s = s.split("\n")[0];
			}

			EventBus.get().post(GenericLogEvent.verbose("Proxy response: " + s));
			if(s.startsWith("HTTP/1.1 200")) {
				EventBus.get().post(GenericLogEvent.verbose("Remote connection confirmed"));
				connectionConfirmed = true;
				connected = true;
				localConnection.onRemoteConnected();
			} else {
				EventBus.get().post(GenericLogEvent.verbose("Connection aborted"));
				socket.close();
				localConnection.onRemoteClosedConnection(null);
			}
		} else {
			super.packetReceived(socket, packet);
		}
	}
}

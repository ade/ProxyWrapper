package se.ade.autoproxywrapper.events;

import se.ade.autoproxywrapper.ProxyMode;

import java.net.InetSocketAddress;

public class DetectModeEvent {
    public final ProxyMode mode;
    public final InetSocketAddress host;

    public DetectModeEvent(ProxyMode mode, InetSocketAddress host) {
        this.mode = mode;
        this.host = host;
    }
}

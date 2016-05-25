package se.ade.autoproxywrapper.loopback;

/**
 * Created by adrnil on 25/05/16.
 */
public class LoopBackConfig {
	private String remoteHost;
	private int remotePort;
	private int localPort;
	private String alias;

	public LoopBackConfig(String remoteHost, int remotePort, int localPort, String alias) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;
		this.alias = alias;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}


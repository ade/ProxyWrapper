package se.ade.autoproxywrapper.config.model;

import javafx.collections.ObservableList;
import se.ade.autoproxywrapper.model.ForwardProxy;

import java.util.ArrayList;
import java.util.List;

public class GsonConfig {

    private List<ForwardProxy> forwardProxies = new ArrayList<>();
	private List<String> blockedHosts = new ArrayList<>();
	private List<String> directModeHosts = new ArrayList<>();
    private int localPort;
    private boolean enabled = true;
	private boolean startMinimized;
    private boolean verboseLogging;
	private boolean blockedHostsEnabled;
	private boolean directModeHostsEnabled;

    public List<ForwardProxy> getForwardProxies() {
        return forwardProxies;
    }

	public List<String> getBlockedHosts() {
		return blockedHosts;
	}

	public List<String> getDirectModeHosts() {
		return directModeHosts;
	}

	public void setForwardProxies(List<ForwardProxy> forwardProxies) {
        this.forwardProxies = forwardProxies;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

	public boolean isStartMinimized() {
		return startMinimized;
	}

	public void setStartMinimized(boolean startMinimized) {
		this.startMinimized = startMinimized;
	}

	public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public void setVerboseLogging(boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
    }

	public void setDirectModeHosts(List<String> items) {
		this.directModeHosts = items;
	}

	public void setBlockedHosts(List<String> blockedHosts) {
		this.blockedHosts = blockedHosts;
	}

	public boolean isBlockedHostsEnabled() {
		return blockedHostsEnabled;
	}

	public void setBlockedHostsEnabled(boolean blockedHostsEnabled) {
		this.blockedHostsEnabled = blockedHostsEnabled;
	}

	public boolean isDirectModeHostsEnabled() {
		return directModeHostsEnabled;
	}

	public void setDirectModeHostsEnabled(boolean directModeHostsEnabled) {
		this.directModeHostsEnabled = directModeHostsEnabled;
	}
}

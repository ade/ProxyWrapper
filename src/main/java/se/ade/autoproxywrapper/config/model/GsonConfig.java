package se.ade.autoproxywrapper.config.model;

import se.ade.autoproxywrapper.model.ForwardProxy;

import java.util.ArrayList;
import java.util.List;

public class GsonConfig {

    private List<ForwardProxy> forwardProxies = new ArrayList<>();

    private int localPort;

    private boolean enabled = true;

	private boolean startMinimized;

    private boolean verboseLogging;

    public List<ForwardProxy> getForwardProxies() {
        return forwardProxies;
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
}

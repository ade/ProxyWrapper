package se.ade.autoproxywrapper.model;

import java.util.ArrayList;
import java.util.List;

public class GsonConfig {

    private List<ForwardProxy> forwardProxies = new ArrayList<>();

    private int localPort;

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

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public void setVerboseLogging(boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
    }
}

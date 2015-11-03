package se.ade.autoproxywrapper.model;

public class ForwardProxy {

    private String host;

    private int port;

    public ForwardProxy() {}

    public ForwardProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

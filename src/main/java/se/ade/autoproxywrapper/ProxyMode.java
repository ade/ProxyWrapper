package se.ade.autoproxywrapper;

public enum ProxyMode {
    DIRECT("Direct"),
    USE_PROXY("Proxied");

    private String name;

    ProxyMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

package se.ade.autoproxywrapper;

public enum ProxyMode {
    AUTO("Auto"),
    DIRECT("Direct"),
    USE_PROXY("Proxied"),
    DISABLED("Disabled");

    private String name;

    ProxyMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

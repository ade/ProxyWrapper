package se.ade.autoproxywrapper;

/**
 * Created by adrnil on 17/12/14.
 */
public enum ProxyMode {
    AUTO("Auto"),
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

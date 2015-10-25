package se.ade.autoproxywrapper;

import se.ade.autoproxywrapper.model.GsonConfig;

public final class Config {

    private static GsonConfig config;

    private Config() {}

    public static GsonConfig config() {
        if(config == null) {
            config = GsonConfigIO.load();
            if(config == null) {
                GsonConfigIO.save(new GsonConfig());
                config = GsonConfigIO.load();
            }
        }
        return config;
    }

    public static void save() {
        GsonConfigIO.save(config);
    }
}

package se.ade.autoproxywrapper.config;

import se.ade.autoproxywrapper.config.model.GsonConfig;

public final class Config {

	private static GsonConfig config;

	private Config() {}

	public static GsonConfig getConfig() {
		if(config == null) {
			if (!GsonConfigIO.configExists()) {
				GsonConfigIO.save(new GsonConfig());
			}
			config = GsonConfigIO.load();
		}
		return config;
	}

	public static void save() {
		GsonConfigIO.save(config);
	}
}

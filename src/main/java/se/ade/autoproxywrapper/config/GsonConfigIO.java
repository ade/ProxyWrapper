package se.ade.autoproxywrapper.config;

import com.google.gson.Gson;
import se.ade.autoproxywrapper.events.*;
import se.ade.autoproxywrapper.config.model.GsonConfig;

import static se.ade.autoproxywrapper.config.UserConfigurationPathUtil.getConfigFilePath;

import java.io.IOException;
import java.nio.file.*;

public class GsonConfigIO {

	private static final String CONFIG_FILE_NAME = "config.json";
	private static final Gson GSON = new Gson();

	public static boolean configExists() {
		Path configFilePath = getConfigFilePath(CONFIG_FILE_NAME);
		return Files.exists(configFilePath);
	}

	public static GsonConfig load() {
		Path configFilePath = getConfigFilePath(CONFIG_FILE_NAME);
		try {
			byte[] bytes = Files.readAllBytes(configFilePath);
			String json = new String(bytes, "UTF-8");
			return GSON.fromJson(json, GsonConfig.class);
		} catch (java.io.IOException e) {
			EventBus.get().post(GenericLogEvent.info("Error: Could not load config " + configFilePath + " (" + e.getMessage() + ")"));
			return null;
		}
	}

	public static void save(GsonConfig gsonConfig) {
		Path configFilePath = getConfigFilePath(CONFIG_FILE_NAME);
		try {
			Files.write(configFilePath, GSON.toJson(gsonConfig).getBytes());
		} catch (IOException e) {
			EventBus.get().post(GenericLogEvent.info("Error: Could not save config " + configFilePath + " (" + e.getMessage() + ")"));
		}
	}
}

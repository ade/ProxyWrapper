package se.ade.autoproxywrapper;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.containsAny;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import com.google.gson.Gson;
import se.ade.autoproxywrapper.events.EventBus;
import se.ade.autoproxywrapper.events.GenericLogEvent;
import se.ade.autoproxywrapper.model.GsonConfig;

public class GsonConfigIO {

	private static final String DATA_PATH_WINDOWS_OLD = System.getProperty("user.home") + "\\Local Settings\\Application Data\\miniproxy\\";
	private static final String DATA_PATH_WINDOWS_NEW = System.getProperty("user.home") + "\\AppData\\Local\\miniproxy\\";
	private static final String DATA_PATH_MAC_OSX = System.getProperty("user.home") + "/Library/Application Support/miniproxy/";
	private static final String DATA_PATH_LINUX = System.getProperty("user.home") + "/.config/miniproxy/";
	private static final String DATA_PATH_OTHER = System.getProperty("user.home") + File.separator + "miniproxy";
	private static final Gson GSON = new Gson();

	public static boolean configExists() {
		Path configFilePath = getConfigFilePath();
		return Files.exists(configFilePath);
	}

	public static GsonConfig load() {
		Path configFilePath = getConfigFilePath();
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
		Path configFilePath = getConfigFilePath();
		try {
			Files.write(configFilePath, GSON.toJson(gsonConfig).getBytes());
		} catch (IOException e) {
			EventBus.get().post(GenericLogEvent.info("Error: Could not save config " + configFilePath + " (" + e.getMessage() + ")"));
		}
	}

	private static Path getConfigFilePath() {
		String osName = System.getProperty("os.name");
		Path NIOPath = null;

		if (contains(osName, "Windows")) {
			if (containsAny(osName, "2000", "2003", "XP")) {
				NIOPath = Paths.get(DATA_PATH_WINDOWS_OLD);
			} else if (containsAny(osName, "2008", "2012", "vista", "7", "8", "8.1", "10")) {
				NIOPath = Paths.get(DATA_PATH_WINDOWS_NEW);
			}
		} else if (contains(osName, "Mac")) {
			NIOPath = Paths.get(DATA_PATH_MAC_OSX);
		} else if (contains(osName, "Linux")) {
			NIOPath = Paths.get(DATA_PATH_LINUX);
		}

		// Fallback to generic path if no OS could be determined
		if (NIOPath == null) {
			NIOPath = Paths.get(DATA_PATH_OTHER);
		}

		if (Files.notExists(NIOPath)) {
			try {
				Files.createDirectories(NIOPath);
			} catch (IOException e) {
				EventBus.get().post(GenericLogEvent.info("Error: Could not create config directory (" + e.getMessage() + ")"));
			}
		}

		return NIOPath.resolve("AutoProxyWrapper.json");
	}

}

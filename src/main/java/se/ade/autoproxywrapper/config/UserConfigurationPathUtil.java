package se.ade.autoproxywrapper.config;

import se.ade.autoproxywrapper.events.*;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.*;
import java.nio.file.*;

public class UserConfigurationPathUtil {

	private static final String DATA_PATH_WINDOWS_OLD = System.getProperty("user.home") + "\\Local Settings\\Application Data\\proxywrapper\\";
	private static final String DATA_PATH_WINDOWS_NEW = System.getProperty("user.home") + "\\AppData\\Local\\proxywrapper\\";
	private static final String DATA_PATH_MAC_OSX = System.getProperty("user.home") + "/Library/Application Support/proxywrapper/";
	private static final String DATA_PATH_LINUX = System.getProperty("user.home") + "/.config/proxywrapper/";
	private static final String DATA_PATH_OTHER = System.getProperty("user.home") + File.separator + "proxywrapper";

	public static Path getConfigFilePath(String fileName) {
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

		return NIOPath.resolve(fileName);
	}

}

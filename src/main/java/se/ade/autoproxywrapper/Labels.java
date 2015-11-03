package se.ade.autoproxywrapper;

import java.util.HashMap;

/**
 * Temporary label solution
 * Created by adrnil on 03/11/15.
 */
public class Labels {
	private static Labels instance;

	public static Labels get() {
		if(instance == null) {
			instance = new Labels();
		}
		return instance;
	}

	private HashMap<String, String> labelMap = new HashMap<>();

	private Labels() {
		add("mode.direct", "Direct");
		add("mode.auto", "Auto");
		add("mode.change-to-direct", "Direct mode");
		add("mode.change-to-auto", "Auto mode");
		add("app.name", "ProxyWrapper");
		add("actions.open-app", "Open ProxyWrapper");
		add("actions.exit", "Exit");
		add("generic.proxy-mode", "Proxy mode");
	}

	private void add(String key, String value) {
		labelMap.put(key, value);
	}

	public String get(String key) {
		if(labelMap.containsKey(key)) {
			return labelMap.get(key);
		} else {
			return "[label error]";
		}
	}
}

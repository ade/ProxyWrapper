package se.ade.autoproxywrapper;

import com.google.common.collect.ImmutableMap;

/**
 * Temporary label solution
 * Created by adrnil on 03/11/15.
 */
public class Labels {
	private static final ImmutableMap<String, String> labelMap = ImmutableMap
			.<String, String>builder()
			.put("mode.direct", "Direct")
			.put("mode.auto", "Auto")
			.put("mode.change-to-direct", "Direct mode")
			.put("mode.change-to-auto", "Auto mode")
			.put("app.name", "ProxyWrapper")
			.put("actions.open-app", "Open ProxyWrapper")
			.put("actions.exit", "Exit")
			.put("generic.proxy-mode", "Proxy mode")
			.build();

	public static String get(String key) {
		if(labelMap.containsKey(key)) {
			return labelMap.get(key);
		} else {
			return "[label error]";
		}
	}
}

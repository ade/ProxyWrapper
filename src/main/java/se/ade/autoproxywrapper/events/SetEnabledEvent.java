package se.ade.autoproxywrapper.events;

import se.ade.autoproxywrapper.ProxyMode;

/**
 * Created by adrnil on 17/12/14.
 */
public class SetEnabledEvent {
    public boolean enabled;

	public SetEnabledEvent(boolean enabled) {
		this.enabled = enabled;
	}
}

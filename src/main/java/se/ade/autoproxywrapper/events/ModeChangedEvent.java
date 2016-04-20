package se.ade.autoproxywrapper.events;

import se.ade.autoproxywrapper.ProxyMode;

/**
 * Created by adrnil on 20/04/16.
 */
public class ModeChangedEvent {
	public ProxyMode mode;

	public ModeChangedEvent(ProxyMode mode) {
		this.mode = mode;
	}
}

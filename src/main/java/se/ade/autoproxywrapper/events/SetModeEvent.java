package se.ade.autoproxywrapper.events;

import se.ade.autoproxywrapper.ProxyMode;

/**
 * Created by adrnil on 17/12/14.
 */
public class SetModeEvent {
    public ProxyMode mode;

    public SetModeEvent(ProxyMode mode) {
        this.mode = mode;
    }
}

package se.ade.autoproxywrapper.events;

import se.ade.autoproxywrapper.ProxyMode;

/**
 * Created by adrnil on 18/12/14.
 */
public class DetectModeEvent {
    public ProxyMode mode;

    public DetectModeEvent(ProxyMode mode) {
        this.mode = mode;
    }
}

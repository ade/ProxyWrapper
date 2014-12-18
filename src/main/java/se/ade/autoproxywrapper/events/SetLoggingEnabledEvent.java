package se.ade.autoproxywrapper.events;

/**
 * Created by adrnil on 18/12/14.
 */
public class SetLoggingEnabledEvent {
    public boolean enabled;

    public SetLoggingEnabledEvent(boolean enabled) {
        this.enabled = enabled;
    }
}

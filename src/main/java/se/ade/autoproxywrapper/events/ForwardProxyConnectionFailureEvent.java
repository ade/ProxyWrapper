package se.ade.autoproxywrapper.events;

/**
 * Created by adrnil on 18/12/14.
 */
public class ForwardProxyConnectionFailureEvent {
    public Throwable error;

    public ForwardProxyConnectionFailureEvent(Throwable error) {
        this.error = error;
    }
}

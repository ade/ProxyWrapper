package se.ade.autoproxywrapper.events;

/**
 * Created by adrnil on 18/12/14.
 */
public class RequestEvent {
    public String method;
    public String url;

    public RequestEvent(String method, String url) {
        this.method = method;
        this.url = url;
    }
}

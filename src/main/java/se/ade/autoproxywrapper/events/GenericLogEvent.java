package se.ade.autoproxywrapper.events;

public class GenericLogEvent {

    private String message;

    public GenericLogEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

package se.ade.autoproxywrapper.events;

public class GenericLogEvent {

    public enum GenericLogEventType {
        INFO, VERBOSE
    }

    private String message;
    private GenericLogEventType type;

    public GenericLogEvent(String message, GenericLogEventType type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isVerbose() {
        return type == GenericLogEventType.VERBOSE;
    }
}

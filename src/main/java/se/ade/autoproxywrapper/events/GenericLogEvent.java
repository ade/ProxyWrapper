package se.ade.autoproxywrapper.events;

public class GenericLogEvent {

    public enum GenericLogEventType {
        INFO, VERBOSE
    }

    public final String message;
    public final GenericLogEventType type;

    public static GenericLogEvent info(String message) {
        return new GenericLogEvent(message, GenericLogEventType.INFO);
    }

    public static GenericLogEvent verbose(String message) {
        return new GenericLogEvent(message, GenericLogEventType.VERBOSE);
    }

    private GenericLogEvent(String message, GenericLogEventType type) {
        this.message = message;
        this.type = type;
    }

    public boolean isVerbose() {
        return type == GenericLogEventType.VERBOSE;
    }
}

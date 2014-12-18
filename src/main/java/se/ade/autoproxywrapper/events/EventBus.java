package se.ade.autoproxywrapper.events;

import java.util.LinkedList;

/**
 * Created by adrnil on 11/12/14.
 */
public class EventBus extends com.google.common.eventbus.EventBus {

    private static EventBus instance;
    public static EventBus get() {
        if(instance == null) {
            instance = new EventBus();
        }

        return instance;
    }
}

package de.neue_phase.asterisk.ClickDial.eventbus;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.eclipse.swt.widgets.Display;

public class EventBusFactory{

    private static AsyncEventBus displayThreadEventBus       = null;
    private final static AsyncEventBus threadPerTaskEventBus = new AsyncEventBus (new ThreadPerTaskExecutor (),
                                                                                  new ExceptionHandler());
    private final static EventBus syncEventBus = new EventBus (new ExceptionHandler());

    /**
     * instantiate the sync event bus, so that events can be executed syncronously in UI thread
     * @param display The main display of the application
     */
    public static void instantiateDisplayEventBus (Display display) {
        displayThreadEventBus = new AsyncEventBus (new DisplayThreadSyncExecutor (display),
                                                   new ExceptionHandler());
    }

    /**
     * this will return an eventbus which will _synchronously_ execute the event in the swt UI thread
     * @return EventBus
     */
    public static AsyncEventBus getDisplayThreadEventBus () {
        return displayThreadEventBus;
    }

    /**
     * This will return an event bus, which will execute the event in a new thread
     * @return EventBus
     */
    public static AsyncEventBus getThreadPerTaskEventBus () {
        return threadPerTaskEventBus;
    }

    /**
     * This will return a synchronous non-swt UI thread
     * @return EventBus
     */
    public static EventBus getSyncEventBus () { return syncEventBus; }
}

package de.neue_phase.asterisk.ClickDial.eventbus;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.eclipse.swt.widgets.Display;

public class EventBusFactory{

    private static AsyncEventBus displayThreadEventBus       = null;
    private final static AsyncEventBus threadPerTaskEventBus = new AsyncEventBus (new ThreadPerTaskExecutor (),
                                                                                  new ExceptionHandler());
    private final static EventBus syncEventBus = new EventBus (new ExceptionHandler());

    public final static void instanciateDisplayEventBus (Display disp) {
        displayThreadEventBus = new AsyncEventBus (new DisplayThreadSyncExecutor (disp),
                                                   new ExceptionHandler());
    }

    public static AsyncEventBus getDisplayThreadEventBus () {
        return displayThreadEventBus;
    }

    public static AsyncEventBus getThreadPerTaskEventBus () {
        return threadPerTaskEventBus;
    }

    public static EventBus getSyncEventBus () {
        return syncEventBus;
    }
}

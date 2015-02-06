package de.neue_phase.asterisk.ClickDial.util;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import de.neue_phase.asterisk.ClickDial.util.events.ClickDialEvent;
import de.neue_phase.asterisk.ClickDial.util.events.FindContactEvent;
import de.neue_phase.asterisk.ClickDial.util.listener.EventListener;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

/*
 * Dispatcher
 * receives messages from all ADA components and delivers them to registered recipients
 */
public class Dispatcher {
    private Display display;
    private HashMap<ClickDialEvent.Type, ArrayList<EventListener>> listeners = new HashMap<ClickDialEvent.Type, ArrayList<EventListener>>();
    private ArrayList<ClickDialEvent> events = new ArrayList<ClickDialEvent>();
    private ArrayList<ClickDialEvent> eventsToDispatch = null;

    private final Logger    log 				= Logger.getLogger(this.getClass());

    public Dispatcher (Display display) {
        this.display = display;
    }

    public void addEventListener(ClickDialEvent.Type type, EventListener listener) {
        ArrayList<EventListener> list = this.listeners.get(type);

        if(list == null)
            list = new ArrayList<EventListener>();

        list.add(listener);
        this.listeners.put(type, list);
    }

    /**
     * dispatchEvent simply puts the event into the event queue
     * Note: Needs to be synchronized, since multiple threads are writing/reading the queue
     * @param event
     *
     */
    public synchronized void  dispatchEvent(ClickDialEvent event) {
        log.debug ("Adding event to event queue: " + event.getClass ().toString ());
        this.events.add(event);
        this.display.wake (); // wake the display since otherwise it would block our dispatch() function from beeing executed
    }

    /**
     * copyEventQueueForDispatching copies all existing events in the event queue to a
     * separate queue for dispatching. This is done to not block the whole queue while
     * posting events.
     */
    protected synchronized void copyEventQueueForDispatching () {
        eventsToDispatch = new ArrayList<ClickDialEvent>(events);
        events = new ArrayList<ClickDialEvent>();
    }

    /**
     * dispatch is beeing called through the monolithic GUI thread and
     * posts every event listed in the event queue to the registered listeners
     * Note: Needs __not__ to be synchronized, since we copy the events in the first step
     */
    public void dispatch () {
        if (this.events.size () == 0)
            return;

        log.debug ("Dispatcher.dispatch() with an event queue size of '"+events.size ()+"'");


        this.copyEventQueueForDispatching (); // synchronized


        for (ClickDialEvent eventToDispatch : this.eventsToDispatch) {
            ArrayList<EventListener> list = this.listeners.get(eventToDispatch.getType());
            if(list == null)
                return; // no listeners registered

            for(EventListener listener : this.listeners.get(eventToDispatch.getType())) {
                log.debug ("Dispatching event '"+eventToDispatch.getPayload ().getClass ().getSimpleName ()+"' to listener '"+listener.getClass ().getSimpleName ()+"'");
                String listenerEventFunctionName = "handle" + eventToDispatch.getClass ().getSimpleName ();
                try {
                    Method listenerEventFunction = listener.getClass ().getMethod (listenerEventFunctionName,
                                                                                   eventToDispatch.getPayload ().getClass ());
                    log.debug ("Found Method '"+listenerEventFunction.toGenericString ()+"'");
                    log.debug ("Event 1: "+eventToDispatch.getPayload ().getClass ());

                    listenerEventFunction.invoke (listener, eventToDispatch);
                }
                catch (NoSuchMethodException e) {
                    log.error ("Can't call " + listenerEventFunctionName + " on listener class " + listener.getClass ().getSimpleName () + " - function not implemented?", e);
                }
                catch (IllegalAccessException e) {
                      log.error ("Can't call " + listenerEventFunctionName + " on listener class " + listener.getClass ().getSimpleName () + " - function not public?", e);
                    log.error (e);
                }
                catch (InvocationTargetException e) {}
            }

        }

    }
}

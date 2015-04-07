package de.neue_phase.asterisk.ClickDial.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import org.apache.log4j.Logger;
import org.asteriskjava.live.CallerId;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.asteriskjava.manager.event.NewExtenEvent;
import org.asteriskjava.manager.event.NewStateEvent;
import org.asteriskjava.manager.event.OriginateResponseEvent;
import org.eclipse.swt.widgets.Display;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ControllerTypes;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants.CallWindowAppearEdges;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants.SettingsTypes;
import de.neue_phase.asterisk.ClickDial.controller.listener.InsufficientSettingsListener;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.controller.util.RWLock;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import de.neue_phase.asterisk.ClickDial.widgets.CallWindow;
import de.neue_phase.asterisk.ClickDial.widgets.util.CallWindowWindowPlacer;

/**
 * CallWindow Controller class is instantiated  once to spawn and
 * keep track of all CallWindow widgets
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class CallWindowController extends ControllerBaseClass implements ManagerEventListener, Runnable {

	private SettingsHolder settings 	= SettingsHolder.getInstance();
	
	/* active windows - Relation "uniqueId" <> "Window" */
	private HashMap<String, CallWindow> windowArray = new HashMap<String, CallWindow>(InterfaceConstants.MaxCallWindowInstances);
	
	/* keep track of used indexes with this list */
	private ArrayList<Integer>			usedIndexes	= new ArrayList<Integer>(InterfaceConstants.MaxCallWindowInstances);
	
	private Display						display			= Display.getCurrent();

	/* EventArray for incoming events and his "lock" object for thread synchronization */
	private final RWLock				newEventLock	= new RWLock();
	private ArrayList<ManagerEvent> 	newEventArray	= new ArrayList<ManagerEvent>();
	
	protected final Logger log 				= Logger.getLogger(this.getClass());
	
	/**
	 * call state enumeration, which will represent the color of the
	 * CallWindow
	 *
	 */
  	public enum			CallState {
  		CONNECTED, DISCONNECTED, RINGING, UNKNOWN, NEW, FAILED
  	}
	
  	/** 
  	 * constructor
  	 */
  	public CallWindowController(SettingsHolder settingsRef, BaseController b) {
		super(settingsRef, b);
		type		= ControllerTypes.CallWindow;
  		schedule(); // -- schedule ourself if someone instantiates us
	}

	/**
	 * create a new CallWindow upon an event
	 * @param uniqueId
	 * @param from
	 * @param to
	 * @param state
	 */
	private void newWindow (String uniqueId, String from, String to, CallState state) {
		
		log.info( " new Call window uniqueId = " + uniqueId + " from = " + from + " to = " + to + " in state = " + state);
		if (uniqueId.isEmpty()) {
			log.error("Can't create window if the uniqueID is empty.");
			return;
		}
		
					
		int index = 1;
		for (; index < InterfaceConstants.MaxCallWindowInstances; index++)
			if ( ! usedIndexes.contains(index) )
				break;

		// -- reserve the index
		usedIndexes.add(index);	

		// -- create the window
		CallWindow w = new CallWindow(   new CallWindowWindowPlacer(edgeConfigToEnum(), index), from, to, state, uniqueId);   
		windowArray.put(uniqueId, w);
	}

	/**
	 * schedule to close a window  
	 * @param uniqueID the unique ID of the request
	 * @param close_meantime Close the window in a specified amount of seconds
	 */
	private void scheduleWindowClose (String uniqueID, int close_meantime) {
		
		CallWindow w = windowArray.get( uniqueID );
		if (w == null)
		{
			log.error ("scheduleWindowClose: Window with unique ID '" + uniqueID + "' not found");
			return;
		}

		w.scheduleClose(close_meantime);
	}

	/** 
	 * get the correct enum value from configuration
	 * @return CallWindowAppearEdges value
	 */
	private CallWindowAppearEdges edgeConfigToEnum () {
		return InterfaceConstants.configStringToEnum(settings.get(SettingsTypes.global).getValue("call_window_from"));
	}

	/** 
	 * measure the CallState Enum value from the String state given by asterisk
	 * @param state
	 * @return
	 */
	private CallState asteriskToOurEnumCallState (String state) {
		if (state.equals("Up"))
			return CallState.CONNECTED;
		else if (state.equals("Ringing") || state.equals("Ring"))
			return CallState.RINGING;
		else if (state.equals("new"))
			return CallState.NEW;
		else if (state == null)
			return CallState.DISCONNECTED;
		
		return CallState.UNKNOWN;
	}
	
	
	/**
	 * the callback function of AsteriskController if an Event reaches the 
	 * AsteriskManagerConnection. It will put the request in the newWindowArray
	 * @param  arg0
	 */
	public void onManagerEvent(ManagerEvent arg0) {
		newEventLock.getWriteLock();
		newEventArray.add(arg0);
		newEventLock.releaseLock();
	}

	
	/**
	 * internal function to handle Manager Events coming from AsteriskController.
	 * This is needed, while the one-Thread architecture of SWT won't allow us
	 * to handle the event directly as it reaches the AsteriskController Callback 
	 * function
	 * @param arg0
	 */
	private void handleManagerEvent (ManagerEvent arg0) {
		if (arg0 instanceof org.asteriskjava.manager.event.NewStateEvent)
		{
			NewStateEvent event = (NewStateEvent) arg0;
			if ( ! ourChannel( event.getChannel() )   )
				return; 

			if (windowArray.containsKey(event.getUniqueId())) {
				/* new state of an existing call Call */
				log.debug("Updateing state of window" + event.getUniqueId() + " to " + event.getState());
				windowArray.get(event.getUniqueId()).updateState (asteriskToOurEnumCallState (event.getState()));
				windowArray.get(event.getUniqueId()).updateFrom (new CallerId(event.getCallerIdName(), event.getCallerIdNum()).toString());
			}
			else  {
				/* new call */
				log.debug("NewStateEvent. Opening new window");
				newWindow(	event.getUniqueId(), 
							new CallerId(event.getCallerIdName(), event.getCallerIdNum()).toString(), 
							"Micha", 
							asteriskToOurEnumCallState(event.getState()));
			}
		}
		else if (arg0 instanceof NewChannelEvent) {
			NewChannelEvent event = (NewChannelEvent) arg0;
			if ( ! ourChannel( event.getChannel() )   )
				return; 
			
			log.debug("NewChannelEvent. Opening new window");
			newWindow(	event.getUniqueId(), 
						new CallerId(event.getCallerIdName(), event.getCallerIdNum()).toString(), 
						"", 
						asteriskToOurEnumCallState(event.getState()));
		}
		else if (arg0 instanceof NewExtenEvent) {
			NewExtenEvent event = (NewExtenEvent) arg0;
			if ( ourChannel( event.getChannel() ) &&
				 windowArray.containsKey(event.getUniqueId())) 
			{
				log.debug("Updateing 'to' of window" + event.getUniqueId() + " to " + event.getExtension());
				windowArray.get(event.getUniqueId()).updateTo (event.getExtension());
				windowArray.get(event.getUniqueId()).updateApp (event.getApplication());
			}
		}
		else if (arg0 instanceof org.asteriskjava.manager.event.HangupEvent)
		{
			HangupEvent event = (HangupEvent) arg0;
			if ( ! ourChannel(event.getChannel()) &&  windowArray.containsKey(event.getUniqueId()) )
				return; 

			log.debug("Hangup Event. Closing " + event.getUniqueId());
			windowArray.get(event.getUniqueId()).updateState (CallState.DISCONNECTED);
			scheduleWindowClose ( event.getUniqueId(), 2 );
		}
		else if ( arg0 instanceof org.asteriskjava.manager.event.OriginateResponseEvent) {
			OriginateResponseEvent event = (OriginateResponseEvent) arg0;
			
			if (event.getResponse().equalsIgnoreCase("Failure")) {
				newWindow(	event.getActionId(), 
							new CallerId(event.getCallerIdName(), event.getCallerIdNum()).toString(), 
							"Micha", 
							CallState.FAILED);
				scheduleWindowClose ( event.getActionId(), 2);
			}

		}
	}

	/**
	 * strip off the random channel id of asterisk
	 * @param rawChannel
	 * @return the channel
	 */
	private String clearChannelIdentifier (String rawChannel) {
		int index = rawChannel.lastIndexOf("-");
		if (index > 0)
			return rawChannel.substring(0, index);
		else
			return rawChannel;
	}
	
	/**
	 * check if the specified channel matches the one specified in the configuration
	 * @param channel
	 * @return if this is the channel we are responsible for
	 */
	private boolean ourChannel (String channel) {
		channel = clearChannelIdentifier(channel);

		if (! channel.equals(settings.get(SettingsTypes.asterisk).getValue("asterisk_channel")))
		{
			log.debug("not our channel: " + channel);
			return false; 
		}
		return	true;
	}
	
	/**
	 * this is the run function, which is called every 500milis 
	 * to spawn new and close old windows
	 */
	public void run() {
		
		// -- debug
		//log.debug("CallWindowController");
		
		// -- check the event queue for new entries
		// - -- this will pop up new windows 
		// - -- this will set new states or attributes of existing CallWindow objects
		newEventLock.getWriteLock();
		for( ManagerEvent e : newEventArray)
			handleManagerEvent(e);

		newEventArray.clear();
		newEventLock.releaseLock();
		
		// -- iterate through the windows and react on "state"	

		Iterator<Entry<String, CallWindow>> i = windowArray.entrySet().iterator();
		Entry<String, CallWindow> e;
		CallWindow				  window;
		ArrayList<CallWindow>     closeMe = new ArrayList<CallWindow>();
		while (i.hasNext()) {
			e 		= i.next();
			window 	= e.getValue();

			switch (window.getState())
			{
			case TOCLOSE: 	if (window.getCloseTime().before(new Date())) {
								log.debug("Window " + window.getIndex() +" has closedate before now: " + window.getCloseTime().toString());
								window.dispose();
							}
							else
								log.debug("Window " + window.getIndex() +" has closedate after now: " + window.getCloseTime().toString() + " now is: " + new Date().toString());
							break;
			case CLOSEABLE:	if ( ! window.dispose()) 
								break; // don't close the window if dispose() was unsuccessful
			case CLOSED:	closeMe.add(window);
							break;
			}
		}
		
		/* finally close the window */
		for (CallWindow w : closeMe ) {
			windowArray.remove(w.getUniqueId());
			usedIndexes.remove(w.getIndex());
			w = null;
		}

		// -- schedule us again
		schedule();
		
	}

	/**
	 * schedule ourselfs every 500 milis
	 */
	public void schedule() {
		display.timerExec(500, this);
	}
	
	/**
	 * 
	 * @return true if this controller controls a widget
	 */
	public boolean isWidgetController() {
		return true;
	}
	
	/* not needed overloaded methods */
	public void registerInsufficientSettingsListener(InsufficientSettingsListener l) {}
	public void startUp() throws InitException {}
	public void closeDown() {}
}

package de.neue_phase.asterisk.ClickDial.widgets;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.boot.Bootstrap;
import de.neue_phase.asterisk.ClickDial.controller.CallWindowController.CallState;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import de.neue_phase.asterisk.ClickDial.widgets.util.CallWindowWindowPlacer;

/**
 *  the transparent window, which should pop up if
 *  a connection appears
 *  
 *  background states
 *  - grey 		= incoming connection
 *  - green 	= established connection
 *  - red		= just terminated or failed connection 
 *  
 *  window fades in and fades out
 *  
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class CallWindow {


    /*
        For ideas how animated call windows can be implemented, please see:
            http://mvnrepository.com/artifact/org.pushing-pixels/trident
            http://www.pushing-pixels.org/2009/06/24/trident-part-7-parallel-timelines-in-swing-and-swt.html
            https://kenai.com/projects/granite/sources/granite/content/src/org/pushingpixels/granite/GraniteUtils.java?rev=8
      */
	
	/**
	 * initialize the hwnd ...
	 * @param handle 
	 * @param transparency
	 */
  	private native static void initModule(int handle, int transparency);
  	
  	/**
  	 * destroy everything native associated with this object
  	 * @param moduleHandle
  	 */
  	private native static void destroyModule(int moduleHandle);
  	
  	/**
  	 * fade the window in
  	 * *WILL BLOCK till window is faded in*
  	 * @param toTrans
  	 * @param steps
  	 * @return 1 if everything is fine, 0 if there is something wrong
  	 */
  	private native static int fadeIn(int handle, int toTrans, int steps);
  	
  	/**
  	 * fade this window out
  	 * *WILL BLOCK till window is faded out*
  	 * @param toTrans
  	 * @param steps
  	 * @return 1 if everything is fine, 0 if there is something wrong
  	 */
  	private native static int fadeOut(int handle, int toTrans, int steps);
  	
  	/**
  	 * returns the currently applied transparency value
  	 * *WILL NOT BLOCK*
  	 * @return transparency (value 0 - 255)or -1 if there is something wrong 
  	 */
  	private native static int getTransparency(int handle);  	
  	
  	private int				transparency	= 0;
  	private Display			display			= Display.getCurrent();
  	private Shell			shell			= new Shell(Bootstrap.primaryShell,  SWT.ON_TOP);
  	private	SettingsHolder  settings		= SettingsHolder.getInstance();
  	
  	/* dependency information */
  	private CallWindowWindowPlacer 	placer 			= null;	// -- index 
  	private String					uniqueId		= "";   // -- asterisk unique ID
  	private Date					closeTime		= null;	// -- holds time to close
  	private CallWindowState			state			= CallWindowState.NEW;
  	
  	private String			from			= "nobody";
  	private String			to				= "nowhere";
  	private CallState		call_state		= CallState.UNKNOWN;

  	/* the labels */
  	private Label			fromLabel		= null;
  	private Label			toLabel			= null;
  	private Label			stateLabel		= null;
  	private Label			timerLabel		= null;
  	private Label			appLabel		= null;
  	
  	/* the logging facility */
	private final Logger    log 				= Logger.getLogger(this.getClass());
	
	/* object state enum */
	public enum CallWindowState {
		NEW, ACTIVE, CLOSEABLE, CLOSED, TOCLOSE  		
	};

	/*
	static {
		System.loadLibrary("CallWindowImplThreaded");
	};
	*/

  	public CallWindow(	CallWindowWindowPlacer placer, 
  						String from, String to, 
  						CallState state,
  						String unique) {
  		
  		this.placer = placer;
  		this.from   = from;
  		this.to		= to;
  		this.call_state  = state;
  		this.uniqueId	 = unique;

  		shell.setSize(InterfaceConstants.CallWindow_size);
  		shell.setLocation(placer.getLocation());
  		
  		// -- initialize the transparency module with the handle (which is a win32 HWND, or a linux ...)
  		
  		log.info ("----------- CALLING initModule for Window " + uniqueId + " with handle " + shell.handle + " -----------");
		//initModule(shell.handle, 0);

		/** 
		 * this should be packed into a function:
		 * 
		 */
		GridData gd 	= new GridData();
		gd.widthHint 	= InterfaceConstants.CallWindow_size.x - 10;

  		shell.setLayout(new GridLayout(1, false));
  		
  		fromLabel = new Label (shell, SWT.NONE | SWT.NO_BACKGROUND);
  		fromLabel.setText("From: " + from);
  		fromLabel.setLayoutData(gd);
  		
  		toLabel = new Label (shell, SWT.NONE);
  		toLabel.setText("To: " + to);
  		toLabel.setLayoutData(gd);
  		
  		// -- TODO: async update of this time here
  		timerLabel = new Label (shell, SWT.NONE | SWT.NO_BACKGROUND);
  		timerLabel.setText("time: ?");
  		timerLabel.setLayoutData(gd);

  		Label l = new Label (shell, SWT.NONE | SWT.NO_BACKGROUND);
  		l.setText("since: " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.GERMANY).format(new Date()));

  		stateLabel = new Label (shell, SWT.NONE  | SWT.NO_BACKGROUND);
  		stateLabel.setText("state: " + call_state.toString());
  		stateLabel.setLayoutData(gd);

  		appLabel = new Label (shell, SWT.NONE  | SWT.NO_BACKGROUND);
  		appLabel.setText("");
  		appLabel.setLayoutData(gd);
  		
  		System.out.println("enter not disposed loop");	

  		changeBackgroundColorByState(state);
  		
		// -- set the transparency to opaque
		// setWindowTransparency(new Integer(settings.get(SettingsTypes.global).getValue("call_window_trans")).intValue());

		shell.open();
		/*fadeIn( shell.handle,
				new Integer(settings.get(SettingsTypes.global).getValue("call_window_trans")).intValue(), 
				10);
		*/
  		// -- dispatch all events
  		while (display.readAndDispatch()) 
  					System.out.println("CallWindow: dispatching");
	}

	/**
	 * dispose all resources
	 */
	private boolean secondDisposeStage () {
		log.debug("secondDisposeStage(): destroy window");
		if (state != CallWindowState.CLOSEABLE) {
			log.debug("secondDisposeStage(): wrong state for that! " + state.toString());
			return false;
		}
		
		// -- destroy all native bindings
		//destroyModule(shell.handle);
		
		// -- dispose the shell
		shell.dispose();

		// -- disposed and ready to get killed
		state = CallWindowState.CLOSED;
		return true;
	}
	
	/**
	 * 1st dispose stage
	 * start the fadeOut thread
	 */
	private void firstDisposeStage () {
		log.debug("firstDisposeStage()");
		
		if (state != CallWindowState.TOCLOSE) {
			log.debug("firstDisposeStage(): wrong state for that! " + state.toString());
			return;
		}
		
		// -- fade the window out (done by a windows thread)
		log.debug("Starting fadeOut Thread");
		/*fadeOut(shell.handle,
				0,
		        15);
		        */
		// -- return false to signal, that this window needs to be disposed later on
		log.debug("dispose: return false");
		state = CallWindowState.CLOSEABLE;
	}

	/** 
	 * the dispose method, which will call 2 stages 
	 * 	- 1st stage will fadeout
	 *  - 2nd will close the window ressources
	 * @return
	 */
	public boolean dispose () {

		switch (closeable()) {
		case -1: return false;
		case 0 : firstDisposeStage(); // fadeOut
				 return false;		  // return false, to say "we are not closed, try this again"
		case 1 : return secondDisposeStage(); // close
		}
		
		return false;
	}

	/**
	 * are we able to be closed ?
	 * @return -1 for not ready / 0 for "fading still in process" / 1 for ready to be closed
	 */
	public short closeable () {
		/*
		int t = getTransparency(shell.handle);
		log.debug("window " + this.getUniqueId() + " closable: " + t);
		if (t == -1)
			return -1; // - not ready
		else if (t > 0)
			return 0; // - still faded int
		else
			return 1; // - ready to be closed
		*/
		return 1;
	}
	
	/**
	 * get placer window index
	 * @return the index of the CallWindow (from the placers perspective)
	 */
	public Integer getIndex () {
		return placer.getIndex();
	}
	/** 
	 * getter for uniqueId
	 * @return the unique id of this CallWindow (coming from asterisk)
	 */
	public String getUniqueId() {
		return uniqueId;
	}
	
	/**
	 * update the call state ~> color traversal
	 * @param new_state
	 */
	public void updateState (CallState new_state) {
		if (new_state != call_state)
		{
			// -- update only if needed
			changeBackgroundColorByState (new_state);
			stateLabel.setText("State: " + new_state.toString());
			call_state = new_state;
		}
	}
	
	/**
	 * update the TO field
	 * @param to
	 */
	public void updateTo (String to) {
		toLabel.setText("To: " + to);
	}

	/**
	 * update the FROM field
	 * @param from
	 */
	public void updateFrom (String from) {
		fromLabel.setText("From: " + from);
	}
	
	
	/**
	 * update the application field
	 * @param app
	 */
	public void updateApp (String app) {
		appLabel.setText("Function: " + app);
	}
	
	/** 
	 * change Color on state
	 * @param state
	 */
	private void changeBackgroundColorByState (CallState state) {
		System.out.println("Changing background to state " + state.toString());
		Color newColor = null;
		
		switch (state) {
		case CONNECTED:		 newColor = new Color(display,0,255,0); // green
								break;
		case DISCONNECTED: 	 newColor = new Color(display,245,132,10); // orange 
								break;
		case RINGING: 		 newColor = new Color(display,245,237,10); // yellow
								break;
		case NEW: 			 newColor = new Color(display,10,245,237); // light-blue
								break;
		case FAILED:		newColor = new Color(display,255,0,0); // red 
							break;
		}

		shell.setBackground(newColor);
		for ( Control c : shell.getChildren() ) {
			System.out.println("Changing background to " + newColor +" on " + c.getClass());
			c.setBackground(newColor);
		}
	}
	
	/**
	 * set our status to "TOCLOSE" and set the closeTime.
	 * External process will close this object then.
	 * @param close_meantime
	 */
	public void scheduleClose (int close_meantime) {

		state = CallWindowState.TOCLOSE;

		GregorianCalendar g = new GregorianCalendar();
		g.add(GregorianCalendar.SECOND, close_meantime);
		closeTime = g.getTime();
		g = null;
	}
	
	/**
	 * getter for the closeTime
	 * @return
	 */
	public Date getCloseTime() {
		return closeTime;
	}
	
	/**
	 * getter for the state
	 * @return CallWindowState which is currently shown by the CallWindow
	 */
	public CallWindowState getState() {
		return state;
	}
	
}

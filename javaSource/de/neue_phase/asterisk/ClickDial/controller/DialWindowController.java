package de.neue_phase.asterisk.ClickDial.controller;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.eventbus.Subscribe;
import de.neue_phase.asterisk.ClickDial.controller.exception.UnknownObjectException;
import de.neue_phase.asterisk.ClickDial.controller.util.AsteriskCallerId;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.ExecuteCTICallEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.TransferClipboardToDialWindowEvent;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerInterface;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerWebservice;
import org.asteriskjava.live.CallerId;
import org.eclipse.jface.fieldassist.*;

import de.neue_phase.asterisk.ClickDial.datasource.Contact;
import de.neue_phase.asterisk.ClickDial.eventbus.events.FindContactEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.FoundContactEvent;
import org.apache.log4j.Logger;
import org.asteriskjava.manager.ManagerConnectionState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;

import de.neue_phase.asterisk.ClickDial.constants.*;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ControllerTypes;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ServiceInterfaceTypes;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import de.neue_phase.asterisk.ClickDial.widgets.DialWindow;

import javax.xml.ws.Service;

/**
 * The DialWindow widget is the main window in this application.
 * This controller will oversee it and gets the events for addressing
 * datasource controller or asterisk controller to dial out, or request
 * some helping numbers/names from the datasources.
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class DialWindowController extends ControllerBaseClass 
implements IContentProposalListener, SelectionListener, IContentProposalListener2, TraverseListener
{

    private AtomicBoolean originatedCallGoing   = new AtomicBoolean (false);
	private DialWindow	dialWindow 			    = null;

	private AtomicBoolean findProposalRunning	= new AtomicBoolean (false);
	
	private final Logger    log 			    = Logger.getLogger(this.getClass());
	
	private AsteriskManagerInterface asCtrl     = null;
	private AsteriskManagerWebservice asWeb     = null;
	
	public DialWindowController(SettingsHolder settingsRef, BaseController b) {
		super(settingsRef, b);
		type		= ControllerTypes.DialWindow;

        try {
            asCtrl = (AsteriskManagerInterface) bC.getServiceInterface (ServiceInterfaceTypes.AsteriskManagerInterface);
            asWeb  = (AsteriskManagerWebservice) bC.getServiceInterface (ServiceInterfaceTypes.Webservice);
        } catch (UnknownObjectException e) {
            log.error ("Critical Internal error - AsteriskManagerInterface/AsteriskManagerWebservice not registered", e);
            bC.bailOut ();
        }
	}
	
	public void startUp () throws InitException  {
		/* we have every setting we want - startup */

		Display.getDefault ().asyncExec (() -> {
			dialWindow = new DialWindow (this);
			dialWindow.startMe ();
		});

        EventBusFactory.getDisplayThreadEventBus ().register (this); // for FoundContactEvents
	}

    @Override
    public void keyTraversed (TraverseEvent traverseEvent) {
        log.debug ("traverse Key typed " + traverseEvent.detail + " return: " + SWT.TRAVERSE_RETURN);
        if (traverseEvent.detail == SWT.TRAVERSE_RETURN && dialWindow.isDialAreaInFocus ()) {
            originateCall (dialWindow.getText ());
        }
    }

    /**
     * Originate a call to the users phone and after connect to another user
     * @param input A number, name - something we can dial to
     */
    private void originateCall (String input) {
        log.debug ("User wants to dial out to: " + input);
        AsteriskCallerId cid = AsteriskCallerId.calleridFromString (input);
        if (cid == null)
            dialWindow.errorOnDialArea ("Invalid target for call origination.");
        else {
            log.debug ("Dial Out Data as CallerId: " + cid.toString ());
            // disable input
            dialWindow.toggleDialAreaState ();

            // trigger call origination by event
            ExecuteCTICallEvent callEvent = new ExecuteCTICallEvent (cid);
            EventBusFactory.getThreadPerTaskEventBus ().post (callEvent);
            Boolean response = callEvent.getReponse (ServiceConstants.WebserviceTimeout);
            if (response) {
                originatedCallGoing.set(true);
                // TODO: show something in the GUI that indicates that an outgoing call is on the way
            }
            dialWindow.toggleDialAreaState ();
        }
    }
    @Override
	public void proposalAccepted (IContentProposal iContentProposal) {
		log.debug ("proposalAccepted");
        findProposalRunning.set(false);
	}

	@Override
	public void proposalPopupClosed (ContentProposalAdapter contentProposalAdapter) {
		log.debug ("proposalPopupClosed");
	}

	@Override
	public void proposalPopupOpened (ContentProposalAdapter contentProposalAdapter) {
		log.debug ("proposalPopupOpened");

        if (!findProposalRunning.get ()) {
            EventBusFactory.getThreadPerTaskEventBus ().post (new FindContactEvent (this.dialWindow.getText ()));
            findProposalRunning.set (true);
        }
    }

    /**
     * @param event the event that contains the resultset
     */
	@Subscribe public void handleFoundContactEvent (FoundContactEvent event) {
		ArrayList<Contact> contacts = event.getContacts ();
		String[] newProposals;

        log.debug ("found "+contacts.size ()+" proposals");

		if (contacts.size () < 1) {
			newProposals =  new String[]{"nothing found.."};
		}
		else {
			ArrayList<String> proposalsAr = new ArrayList<String> ();
			for (Contact contact : contacts)
                proposalsAr.addAll (contact.getStringRepresentation ());

			newProposals = proposalsAr.toArray (new String[proposalsAr.size ()]);
		}

		log.debug ("Updating Autocomplete proposals.");
		dialWindow.updateAutocompleteProposals (newProposals);
        findProposalRunning.set (false);
	}

    /**
     * not used
     * @param arg0
     */
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	/**
	 * implementing SelectionListener
	 *  - called when DialWindow right Click menu is opened 
	 *    and some item is clicked
     * @param arg0 the event
	 */
	public void widgetSelected(SelectionEvent arg0) {
		log.debug("Clicked a MenuItem : " + ((MenuItem) arg0.widget).getText());
		String text = ((MenuItem) arg0.widget).getText();
		if (text.equals("exit"))
		{
			bC.bailOut();
		}
		else if (text.equals("open configuration")) {
            try {
                SettingsController settings = (SettingsController) bC.getController (ControllerTypes.Settings);
                settings.openAsteriskSettingsWindow ();
            } catch (UnknownObjectException e) {
                log.error ("SettingsController not registered - can't show settings window.", e);
            }
		}
		else if (text.equals("about")) {
			/* start splash screen in 'about' mode */
			// SplashScreen splash = new SplashScreen(1);
			// splash.open();
		}

	}

    @Subscribe public void handleTransferClipboardEvent (TransferClipboardToDialWindowEvent event) {
        dialWindow.setText (event.getTransferString ());
    }


    /* IDE says "never used?"
	public String getAsteriskConnectionState () {
		return asCtrl.getUsedUsername() +  ": " + asCtrl.getState().toString();
	}
	
	public String getAsteriskConnectionStageIcon () {
		if ( asCtrl.getState() == ManagerConnectionState.CONNECTED )
			return InterfaceConstants.DialWindow_green_lamp_icon;
		else
			return InterfaceConstants.DialWindow_red_lamp_icon;
	}

	public String getWebserviceConnectionStateIcon () {
		if ( asWeb.isAuthenticated () )
			return InterfaceConstants.DialWindow_green_lamp_icon;
		else
			return InterfaceConstants.DialWindow_red_lamp_icon;
	}

	public String getWebserviceConnectionState () {
		return asCtrl.getUsedUsername();
	}
    */

	/*
	 * close every resources
	 */
	public void closeDown () {
		dialWindow.dispose();
	}


	/**
	 * toogles the visible state of the widget on/off
	 */
	public void toggleHideShowDialWindow () {
		dialWindow.toggleHideShow();
	}
	
	/**
	 * 
	 * @return true if this controller controls a widget
	 */
	public boolean isWidgetController() {
		return true;
	}
}

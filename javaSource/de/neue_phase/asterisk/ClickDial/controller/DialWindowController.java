package de.neue_phase.asterisk.ClickDial.controller;

import java.util.ArrayList;

import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerInterface;
import de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerWebservice;
import de.neue_phase.asterisk.ClickDial.util.events.ClickDialEvent;
import org.eclipse.jface.fieldassist.*;

import de.neue_phase.asterisk.ClickDial.datasource.Contact;
import de.neue_phase.asterisk.ClickDial.util.Dispatcher;
import de.neue_phase.asterisk.ClickDial.util.events.FindContactEvent;
import de.neue_phase.asterisk.ClickDial.util.events.FoundContactEvent;
import de.neue_phase.asterisk.ClickDial.util.listener.FoundContactEventListener;
import org.apache.log4j.Logger;
import org.asteriskjava.manager.ManagerConnectionState;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;

import de.neue_phase.asterisk.ClickDial.constants.*;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ControllerTypes;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import de.neue_phase.asterisk.ClickDial.widgets.DialWindow;

/**
 * The DialWindow widget is the main window in this application.
 * This controller will oversee it and gets the events for addressing
 * datasource controller or asterisk controller to dial out, or request
 * some helping numbers/names from the datasources.
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class DialWindowController extends ControllerBaseClass 
implements FoundContactEventListener, IContentProposalListener, SelectionListener, IContentProposalListener2
{

	private Display     displayRef 			= Display.getCurrent();
	private Dispatcher  dispatcherRef		= null;
	private DialWindow	dialWindow 			= null;

	private	Boolean findProposalRunning		= false;
	
	private final Logger    log 			= Logger.getLogger(this.getClass());
	
	private AsteriskManagerInterface asCtrl = null;
	private AsteriskManagerWebservice asWeb = null;
	
	public DialWindowController(SettingsHolder settingsRef, BaseController b, Dispatcher dispatcherRef) {
		super(settingsRef, b);
		type		= ControllerTypes.DialWindow;
		asCtrl = (AsteriskManagerInterface) bC.getServiceInterface (ControllerConstants.ServiceInterfaceTypes.AsteriskManagerInterface);
		asWeb  = (AsteriskManagerWebservice) bC.getServiceInterface (ControllerConstants.ServiceInterfaceTypes.Webservice);
		this.dispatcherRef = dispatcherRef;
	}
	
	public void startUp () throws InitException  {
		/* we have every setting we want - startup */
		
		dialWindow = new DialWindow(displayRef, this);
		dialWindow.startMe();
		dispatcherRef.addEventListener (ClickDialEvent.Type.ClickDial_FoundContactEvent, this);
	}


	@Override
	public void proposalAccepted (IContentProposal iContentProposal) {
		log.debug ("proposalAccepted");
		synchronized (this.findProposalRunning) {
			this.findProposalRunning = false;
		}
	}

	@Override
	public void proposalPopupClosed (ContentProposalAdapter contentProposalAdapter) {
		log.debug ("proposalPopupClosed");
	}

	@Override
	public void proposalPopupOpened (ContentProposalAdapter contentProposalAdapter) {
		log.debug ("proposalPopupOpened");
		synchronized (this.findProposalRunning)
		{
			if (!this.findProposalRunning) {
				dispatcherRef.dispatchEvent (new FindContactEvent (this.dialWindow.getText ()));
				this.findProposalRunning = true;
			}
		}
	}


	public void handleFoundContactEvent (FoundContactEvent event) {
		ArrayList<Contact> contacts = event.getContacts ();
		String[] newProposals = null;

		if (contacts.size () < 1) {
			newProposals =  new String[]{"nothing found.."};
		}
		else {
			ArrayList<String> proposalsAr = new ArrayList<String> ();
			for (Contact contact : event.getContacts ())
				proposalsAr.addAll (contact.getStringRepresentation ());

			newProposals = proposalsAr.toArray (new String[proposalsAr.size ()]);
		}

		log.debug ("Updating Autocomplete proposals.");
		dialWindow.updateAutocompleteProposals (newProposals);
		synchronized (this.findProposalRunning) {
			this.findProposalRunning = false;
		}
	}

	public void widgetDefaultSelected(SelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * implementing SelectionListener
	 *  - called when DialWindow right Click menu is opened 
	 *    and some item is clicked 
	 */
	public void widgetSelected(SelectionEvent arg0) {
		log.debug("Clicked a MenuItem : " + ((MenuItem) arg0.widget).getText());
		String text = ((MenuItem) arg0.widget).getText();
		if (text.equals("exit"))
		{
			bC.bailOut();
		}
		else if (text.equals("open configuration")) {
			bC.startSettingsProducer(ControllerTypes.DialWindow);
		}
		else if (text.equals("about")) {
			/* start splash screen in 'about' mode */
			// SplashScreen splash = new SplashScreen(1);
			// splash.open();
		}

	}

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

/**
 * 
 */
package de.neue_phase.asterisk.ClickDial.controller;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TrayItem;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ControllerTypes;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import de.neue_phase.asterisk.ClickDial.widgets.TrayIcon;

/**
 * 
 * This TrayIconController should watch over the tray icon, which
 * is used to pop on the settings per menu or (left clicked) pop
 * on the dial window 
 * 
 * @author Michael Konietzny <michael@konietzny.at>
 * @since 26.01.2008
 */
public class TrayIconController extends ControllerBaseClass implements
		ControllerInterface, MenuDetectListener, SelectionListener {
	
	private TrayIcon icon	= null;
	// private String[] menuItems = 
	
	private final Logger    log 			= Logger.getLogger(this.getClass());
	/**
	 * @param settingsRef
	 * @param b
	 */
	public TrayIconController(SettingsHolder settingsRef, BaseController b) {
		super(settingsRef, b);
		type = ControllerTypes.TrayIcon;
		
		icon = new TrayIcon(this, new String[]{"open configuration", "..."} );
	}


	/* (non-Javadoc)
	 * @see de.neue_phase.asterisk.ClickDial.controller.ControllerInterface#isWidgetController()
	 */
	public boolean isWidgetController() {
		return true;
	}

	/**
	 * callback on MenuDetected event of the TrayItem in TrayIcon class
	 * @param arg0
	 */
	public void menuDetected(MenuDetectEvent arg0) {
		icon.showMenu();
	}

	/**
	 * close every resources
	 */
	public void closeDown () {
		icon.dispose();
	}


	public void widgetDefaultSelected(SelectionEvent arg0) {}


	/**
	 * callback on a clicked MenuItem of the TrayItem in TrayIcon class
	 * or on the TrayItem itself
	 * @see de.neue_phase.asterisk.ClickDial.widgets.TrayIcon
	 */
	public void widgetSelected(SelectionEvent arg0) {
		log.debug("widgetSelected: " + arg0 + " Widget " + arg0.widget);
		
		if (arg0.widget instanceof TrayItem) {
			/* tray item code  - show the dialWindow Controller*/
			((DialWindowController) bC.getController(ControllerTypes.DialWindow)).toggleHideShowDialWindow();
		}
		else {
			/* menu code */
			
		}
	}


	
}

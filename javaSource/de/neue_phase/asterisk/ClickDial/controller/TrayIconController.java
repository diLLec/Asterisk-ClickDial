/**
 * 
 */
package de.neue_phase.asterisk.ClickDial.controller;

import com.google.common.eventbus.Subscribe;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants.WorkstateTypes;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.SetWorkstateEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.UpdateWorkstateEvent;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.*;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ControllerTypes;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import de.neue_phase.asterisk.ClickDial.widgets.TrayIcon;
import org.eclipse.swt.widgets.MenuItem;

import java.lang.reflect.Method;
import java.util.HashMap;

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
	private final HashMap<String,Method> trayFunctions = new HashMap<> ();

	
	private final Logger    log 			= Logger.getLogger(this.getClass());
	/**
	 * @param settingsRef
	 * @param b
	 */
	public TrayIconController(SettingsHolder settingsRef, BaseController b) {
		super(settingsRef, b);
		type = ControllerTypes.TrayIcon;

        try {
            trayFunctions.put ("open configuration", TrayIconController.class.getDeclaredMethod ("trayFuncOpenConfiguration"));
            trayFunctions.put ("quit", TrayIconController.class.getDeclaredMethod ("trayFuncQuit"));
        }
        catch (NoSuchMethodException ex) {
                log.error ("Failed to wire tray icon function to class method.", ex);
        }

		icon = new TrayIcon(this,
                            trayFunctions.keySet ().toArray (new String[trayFunctions.size ()]) );
        EventBusFactory.getDisplayThreadEventBus ().register (icon);
        EventBusFactory.getDisplayThreadEventBus ().register (this);
	}

    /**
     * TrayIcon Function: open the configuration Window
     */
    private void trayFuncOpenConfiguration () {
        ((SettingsController) bC.getController (ControllerTypes.Settings)).openSettingsWindow ();
    }

    /**
     * TrayIcon Function: quit application
     */
    private void trayFuncQuit () {
        bC.bailOut ();
    }


    /**
     * returns if this controller controles a widget
     * @return yes/no
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
		else if (arg0.widget instanceof MenuItem) {
            MenuItem menuItem = ((MenuItem) arg0.widget);
            Method m;
            if ((m = trayFunctions.get (menuItem.getText ())) != null) {
                try {
                    m.invoke (this);
                } catch (Exception e) { log.error ("Error while invoking tray function"); }
            }

            if (menuItem.getText ().equals (WorkstateTypes.Feierabend.toString ())) {
                EventBusFactory.getThradPerTaskEventBus ().post (new SetWorkstateEvent (WorkstateTypes.Feierabend));
            }
            else if (menuItem.getText ().equals (WorkstateTypes.Arbeit.toString ())) {
                EventBusFactory.getThradPerTaskEventBus ().post (new SetWorkstateEvent (WorkstateTypes.Arbeit));
            }
            else if (menuItem.getText ().equals (WorkstateTypes.Pause.toString ())) {
                EventBusFactory.getThradPerTaskEventBus ().post (new SetWorkstateEvent (WorkstateTypes.Pause));
            }
            else if (menuItem.getText ().equals (WorkstateTypes.AusserHaus.toString ())) {
                EventBusFactory.getThradPerTaskEventBus ().post (new SetWorkstateEvent (WorkstateTypes.AusserHaus));
            }
		}
	}

    @Subscribe public void onUpdateWorkstateEvent (UpdateWorkstateEvent event) {
        icon.updateTrayIconByWorkstate (event.getTargetWorkstate ());
    }


    public void popupInformation (String message) {
        icon.popupMessage (message, SWT.BALLOON | SWT.ICON_INFORMATION);
    }

    public void popupError (String message) {
        icon.popupMessage (message, SWT.BALLOON | SWT.ICON_ERROR);
    }

    public void popupWarning (String message) {
        icon.popupMessage (message, SWT.BALLOON | SWT.ICON_WARNING);
    }

	
}

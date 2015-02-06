package de.neue_phase.asterisk.ClickDial.controller;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.controller.listener.InsufficientSettingsListener;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;


/**
 * Inverface class for every controller
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public interface ControllerInterface {

	public void startUp () throws InitException;
	public ControllerConstants.ControllerTypes getName ();
	public void registerInsufficientSettingsListener ( InsufficientSettingsListener l );
	public void closeDown () ;
	public boolean isWidgetController();
}

package de.neue_phase.asterisk.ClickDial.controller;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import org.apache.log4j.Logger;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ControllerTypes;
import de.neue_phase.asterisk.ClickDial.controller.listener.InsufficientSettingsListener;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;

/**
 * Abstract class for every controller
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

abstract class ControllerBaseClass implements ControllerInterface {

	protected ControllerTypes type								= ControllerTypes.none;
	protected InsufficientSettingsListener iSettingsListener  	= null;
	protected SettingsHolder settingsRef						= null;
	
	protected BaseController bC									= null;
	
	protected final Logger log 									= Logger.getLogger(this.getClass());
	
	public ControllerBaseClass(SettingsHolder settingsRef, BaseController b) {
		this.settingsRef 	= settingsRef;
		this.bC 			= b;
	}
	
	public ControllerConstants.ControllerTypes getName() {
		return this.type;
	}

	public void setType(ControllerTypes type) {
		this.type = type;
	}
	/**  
	 * register the insufficient settings listener
	 * @param l
	 */
	public void registerInsufficientSettingsListener(
			InsufficientSettingsListener l) {
		log.debug("registered InsufficientSettingsListener "+ l);
		iSettingsListener = l;
	}

	public void startUp() throws InitException {
		return;
	}

	public void closeDown () {
		return;	
	}
	
}

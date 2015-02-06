package de.neue_phase.asterisk.ClickDial.controller.listener;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants.ControllerTypes;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ISettingsExtractModel;

/**
 * Interface that must be implemented to be a valid "pop up the settings
 * window" controller
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public interface InsufficientSettingsListener {
	public ISettingsExtractModel startSettingsProducer(ControllerTypes type);
	public void    bailOut();
}

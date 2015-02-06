package de.neue_phase.asterisk.ClickDial.controller.listener;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ISettingsExtractModel;

public interface InsufficientServiceAuthenticationDataListener {
    public ISettingsExtractModel startSettingsProducer(ControllerConstants.ServiceInterfaceTypes type, Integer tryCount);
    public void acknowledgeLoginData ();
}

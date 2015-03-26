package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ExtractWebserviceAuthData;


public class WebserviceInsufficientAuthDataEvent extends ServiceInsufficientAuthDataEvent <ExtractWebserviceAuthData>  {
    public WebserviceInsufficientAuthDataEvent (ControllerConstants.ServiceInterfaceTypes type, Integer authTry) {
        super (type, authTry);
    }
}

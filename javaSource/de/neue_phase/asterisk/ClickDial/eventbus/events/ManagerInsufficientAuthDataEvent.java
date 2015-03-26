package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.settings.extractModels.ExtractAsteriskManagerInterfaceAuthData;

public class ManagerInsufficientAuthDataEvent extends ServiceInsufficientAuthDataEvent <ExtractAsteriskManagerInterfaceAuthData> {
    public ManagerInsufficientAuthDataEvent (ControllerConstants.ServiceInterfaceTypes type, Integer authTry) {
        super (type, authTry);
    }
}

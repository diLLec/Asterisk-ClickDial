package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;

class ServiceInsufficientAuthDataEvent<T> extends AsyncCallWaitEvent<T> {
    private ControllerConstants.ServiceInterfaceTypes type;
    private Integer authTry;

    public ServiceInsufficientAuthDataEvent (ControllerConstants.ServiceInterfaceTypes type, Integer authTry) {
        this.type = type;
        this.authTry = authTry;
    }

    public ControllerConstants.ServiceInterfaceTypes getType () {
        return type;
    }

    public Integer getAuthTry () {
        return authTry;
    }

}

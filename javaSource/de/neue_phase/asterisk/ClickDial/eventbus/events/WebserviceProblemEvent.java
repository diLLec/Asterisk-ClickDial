package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;

public class WebserviceProblemEvent extends ServiceProblemEvent<Boolean> {
    public WebserviceProblemEvent (ControllerConstants.ServiceInterfaceTypes type, ControllerConstants.ServiceInterfaceProblems problemType) {
        super(type, problemType);
    }
}

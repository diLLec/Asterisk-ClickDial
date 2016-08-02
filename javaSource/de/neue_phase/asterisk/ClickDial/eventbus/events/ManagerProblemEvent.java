package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;

public class ManagerProblemEvent extends ServiceProblemEvent<Boolean> {
    public ManagerProblemEvent (ControllerConstants.ServiceInterfaceTypes type, ControllerConstants.ServiceInterfaceProblems problemType) {
        super(type, problemType);
    }
}

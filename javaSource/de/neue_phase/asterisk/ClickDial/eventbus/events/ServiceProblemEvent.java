package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;

class ServiceProblemEvent<T> extends AsyncCallWaitEvent<T> {
    private ControllerConstants.ServiceInterfaceProblems problemType;
    private ControllerConstants.ServiceInterfaceTypes type;


    public ServiceProblemEvent (ControllerConstants.ServiceInterfaceTypes type, ControllerConstants.ServiceInterfaceProblems problemType) {
        this.problemType = problemType;
        this.type = type;
    }

    public ControllerConstants.ServiceInterfaceTypes getType () {
        return type;
    }

    public ControllerConstants.ServiceInterfaceProblems getProblemType () {
        return problemType;
    }

}

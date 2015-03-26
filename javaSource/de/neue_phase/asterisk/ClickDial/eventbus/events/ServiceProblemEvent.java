package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;

class ServiceProblemEvent<T> extends AsyncCallWaitEvent<T> {
    private ControllerConstants.ServiceInterfaceProblems problemType;
    private Integer problemTry;
    private ControllerConstants.ServiceInterfaceTypes type;


    public ServiceProblemEvent (ControllerConstants.ServiceInterfaceTypes type, ControllerConstants.ServiceInterfaceProblems problemType, Integer problemTry) {
        this.problemType = problemType;
        this.problemTry = problemTry;
        this.type = type;
    }

    public ControllerConstants.ServiceInterfaceTypes getType () {
        return type;
    }

    public ControllerConstants.ServiceInterfaceProblems getProblemType () {
        return problemType;
    }

    public Integer getProblemTry () {
        return problemTry;
    }
}

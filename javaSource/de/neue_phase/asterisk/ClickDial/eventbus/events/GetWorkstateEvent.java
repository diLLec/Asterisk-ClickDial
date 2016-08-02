package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;

public class GetWorkstateEvent extends AsyncCallWaitEvent<String> {
    private InterfaceConstants.WorkstateTypes currentWorkstate;

    public GetWorkstateEvent () {
    }

    /**
     * overridden setResponse, which automatically casts the type to the enum value
     * @param response the response, which contains the workstate string
     */
    @Override
    public void setResponse (String response) {
        super.setResponse (response);
        try {
            this.currentWorkstate = InterfaceConstants.WorkstateTypes.valueOf (response);
        } catch (IllegalArgumentException e) {
            e.printStackTrace ();
        }
    }

    /**
     *
     * @return the workstate as an enum
     */
    public InterfaceConstants.WorkstateTypes getCurrentWorkstate () {
        return currentWorkstate;
    }
}


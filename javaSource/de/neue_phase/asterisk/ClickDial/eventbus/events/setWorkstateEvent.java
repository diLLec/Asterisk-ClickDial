package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;

public class SetWorkstateEvent {
    private InterfaceConstants.WorkstateTypes targetWorkstate;

    public SetWorkstateEvent (InterfaceConstants.WorkstateTypes targetWorkstate) {
        this.targetWorkstate = targetWorkstate;
    }

    public InterfaceConstants.WorkstateTypes getTargetWorkstate () {
        return targetWorkstate;
    }
}

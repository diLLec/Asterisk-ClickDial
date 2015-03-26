package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;

public class UpdateWorkstateEvent extends SetWorkstateEvent {
    public UpdateWorkstateEvent (InterfaceConstants.WorkstateTypes targetWorkstate) {
        super(targetWorkstate);
    }
}

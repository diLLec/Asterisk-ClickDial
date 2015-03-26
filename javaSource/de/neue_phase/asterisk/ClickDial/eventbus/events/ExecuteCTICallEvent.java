package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.controller.util.AsteriskCallerId;

public class ExecuteCTICallEvent extends AsyncCallWaitEvent<Boolean> {
    private AsteriskCallerId target;

    public ExecuteCTICallEvent (AsteriskCallerId target ) {
        this.target = target;
    }

    public String getNumberToCall () {
        return this.target.getNumber ();
    }

    public String getTargetCallerId () {
        return this.target.toString ();
    }
}

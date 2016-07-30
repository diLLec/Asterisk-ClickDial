package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.controller.util.AsteriskCallerId;

public class CallWindowNewCallEvent {
    private AsteriskCallerId fromCid;
    private String uniqueId;

    public CallWindowNewCallEvent (AsteriskCallerId fromCid, String uniqueId) {
        this.uniqueId = uniqueId;
        this.fromCid = fromCid;
    }

    public AsteriskCallerId getFromCid () {
        return fromCid;
    }

    public String getUniqueId () {
        return uniqueId;
    }
}

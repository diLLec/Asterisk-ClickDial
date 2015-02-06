package de.neue_phase.asterisk.ClickDial.util.events;

/**
 * Created by mky on 17.01.2015.
 */
public class ExecuteCTICallEvent extends ClickDialEvent {
    private String numberToCall;
    private final String listenerEventFunction = "handleExecuteCallEvent";

    public ExecuteCTICallEvent (String numberToCall) {
        super(Type.ClickDial_ExecuteCTICallEvent);
        this.numberToCall = numberToCall;
        this.setPayload (this);
    }

    public String getNumberToCall () {
        return this.numberToCall;
    }
}

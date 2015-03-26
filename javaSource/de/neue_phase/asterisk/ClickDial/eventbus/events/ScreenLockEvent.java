package de.neue_phase.asterisk.ClickDial.eventbus.events;

/**
 * Created by mky on 17.01.2015.
 */
public class ScreenLockEvent extends ClickDialEvent {
    private Boolean lockState;
    private final String listenerEventFunction = "handleScreenLockEvent";

    public ScreenLockEvent (Boolean lockState) {
        super(Type.ClickDial_ScreenLockEvent);
        this.lockState = lockState;
        this.setPayload (this);
    }

    public Boolean getLockState () {
        return this.lockState;
    }
}
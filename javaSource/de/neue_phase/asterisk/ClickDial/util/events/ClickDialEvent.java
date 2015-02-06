package de.neue_phase.asterisk.ClickDial.util.events;

/*
 * ClickDialEvent
 * base class for dispatchable events
 */
public abstract class ClickDialEvent {
	public static enum Type {
		ClickDial_ExecuteCTICallEvent,
		ClickDial_ScreenLockEvent,
		ClickDial_FindContactEvent,
		ClickDial_FoundContactEvent,
		ClickDial_SettingsUpdatedEvent
	}

	protected Type type;
	protected ClickDialEvent payload;
	
	public ClickDialEvent (Type type) {
		this.type = type;
	}

	public void setPayload (ClickDialEvent payload) {
		this.payload = payload;
	}

	public Type getType() {
		return this.type;
	}

	public ClickDialEvent getPayload() {
		return this.payload;
	}
}
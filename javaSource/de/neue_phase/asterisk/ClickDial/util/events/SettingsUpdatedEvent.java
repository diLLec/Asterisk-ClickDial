package de.neue_phase.asterisk.ClickDial.util.events;

import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;

import java.util.ArrayList;

public class SettingsUpdatedEvent extends ClickDialEvent {

    private ArrayList<SettingsConstants.SettingsTypes> updatedTypes = null;

    public SettingsUpdatedEvent (ArrayList<SettingsConstants.SettingsTypes> updatedTypes) {
        super(Type.ClickDial_SettingsUpdatedEvent);
        this.updatedTypes = updatedTypes;
        this.setPayload (this);
    }

    public ArrayList<SettingsConstants.SettingsTypes> getUpdatedTypes () {
        return this.updatedTypes;
    }
}

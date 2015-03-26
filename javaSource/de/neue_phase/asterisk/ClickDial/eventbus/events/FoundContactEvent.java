package de.neue_phase.asterisk.ClickDial.eventbus.events;

import de.neue_phase.asterisk.ClickDial.datasource.Contact;

import java.util.ArrayList;

/**
 * Created by mky on 21.01.2015.
 */
public class FoundContactEvent extends ClickDialEvent {
    private ArrayList<Contact> foundContacts;

    public FoundContactEvent (ArrayList<Contact> foundContacts) {
        super(ClickDialEvent.Type.ClickDial_FoundContactEvent);
        this.setPayload (this);
        this.foundContacts = foundContacts;
    }

    public ArrayList<Contact> getContacts () {
        return this.foundContacts;
    }
}


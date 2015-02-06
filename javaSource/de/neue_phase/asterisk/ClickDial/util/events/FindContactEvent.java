package de.neue_phase.asterisk.ClickDial.util.events;

import java.lang.reflect.Method;

public class FindContactEvent extends ClickDialEvent {
    private String searchString;
    private final String listenerEventFunction = "handleFindContactsEvent";

    public FindContactEvent (String searchString) {
        super(ClickDialEvent.Type.ClickDial_FindContactEvent);
        this.searchString = searchString;
        this.setPayload (this);
    }

    public String getSearchString () {
        return this.searchString;
    }
}

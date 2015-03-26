package de.neue_phase.asterisk.ClickDial.eventbus.events;

public class FindContactEvent extends ClickDialEvent {
    private String searchString;
    public FindContactEvent (String searchString) {
        super(ClickDialEvent.Type.ClickDial_FindContactEvent);
        this.searchString = searchString;
        this.setPayload (this);
    }

    public String getSearchString () {
        return this.searchString;
    }
}

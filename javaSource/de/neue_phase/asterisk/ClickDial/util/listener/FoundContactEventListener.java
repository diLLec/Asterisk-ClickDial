package de.neue_phase.asterisk.ClickDial.util.listener;

import de.neue_phase.asterisk.ClickDial.util.events.FoundContactEvent;

public interface FoundContactEventListener extends EventListener {
    public void handleFoundContactEvent (FoundContactEvent event);
}

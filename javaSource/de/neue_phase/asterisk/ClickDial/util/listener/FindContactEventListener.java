package de.neue_phase.asterisk.ClickDial.util.listener;

import de.neue_phase.asterisk.ClickDial.util.events.FindContactEvent;

/**
 * Created by mky on 21.01.2015.
 */
public interface FindContactEventListener extends EventListener {
    public void handleFindContactEvent (FindContactEvent event);
}

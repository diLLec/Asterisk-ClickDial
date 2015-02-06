package de.neue_phase.asterisk.ClickDial.util.listener;

import de.neue_phase.asterisk.ClickDial.util.events.SettingsUpdatedEvent;

public interface SettingsUpdatedListener extends EventListener {
    public void handleSettingsUpdatedEvent (SettingsUpdatedEvent event);
}
package de.neue_phase.asterisk.ClickDial.eventbus.events;

public class TransferClipboardToDialWindowEvent {
    String transferString = "";

    public TransferClipboardToDialWindowEvent (String transferString) {
        this.transferString = transferString;
    }

    public String getTransferString () {
        return transferString;
    }
}

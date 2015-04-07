package de.neue_phase.asterisk.ClickDial.controller;

import com.google.common.eventbus.Subscribe;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.SettingsUpdatedEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.TransferClipboardToDialWindowEvent;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.io.IOException;


public class HotkeyController extends ControllerBaseClass implements HotkeyListener, ClipboardOwner {

    private JIntellitype intelliInstance = null;

    public HotkeyController (SettingsHolder settingsRef, BaseController b) {
        super (settingsRef, b);
    }

    @Override
    public void startUp () throws InitException {
        // init intellitype here to make sure we are not freeing stale resources later
        if (!JIntellitype.isJIntellitypeSupported()) {
            log.error("JIntellitype.DLL was not found in the path or this is not a Windows OS.");
            throw new InitException ("JIntellitype can't be loaded.");
        }

        intelliInstance = JIntellitype.getInstance();
        intelliInstance.addHotKeyListener (this);

        EventBusFactory.getDisplayThreadEventBus ().register (this);
    }

    @Override
    public void closeDown () {
        JIntellitype.getInstance().cleanUp ();
    }

    @Subscribe
    public void onSettingsUpdatedEvent (SettingsUpdatedEvent event) {
        if (event.getUpdatedTypes ().contains (SettingsConstants.SettingsTypes.global)) {
            intelliInstance.unregisterHotKey (1);
            registerHotkeys ();
        }
    }

    /**
     * register the hotkey
     */
    protected void registerHotkeys() {
        try {
            intelliInstance.registerHotKey (1, // hotkey 1
                                            SettingsHolder.getInstance ().get (SettingsConstants.SettingsTypes.global).getValue ("text_selection_call_hk"));
        } catch (Exception e) {
            log.error ("Failed to register Hotkeys.");
        }
    }

    /**
     * EventHandler for Hotkey pressing
     * @param KeyId
     */
    public void onHotKey(int KeyId) {
        log.debug("Hotkey "+ KeyId +" pressed.");

        if (KeyId == 1) {


            // emulate ctrl-c keypress to copy marked text to system clipboard
            try {
                Robot rob = new Robot();
                rob.keyPress(KeyEvent.VK_CONTROL);
                rob.keyPress(KeyEvent.VK_C);
                rob.keyRelease(KeyEvent.VK_C);
                rob.keyRelease(KeyEvent.VK_CONTROL);
            } catch (AWTException ex) {
                log.error(ex);
            }

            String data = getData ();
            log.debug ("Transfering Clipboard Data to dial window (data = '"+ data +"')");
            EventBusFactory.getDisplayThreadEventBus().post (new TransferClipboardToDialWindowEvent (data));
        }
    }

    @Override
    public boolean isWidgetController () {
        return false;
    }

    @Override
    public void lostOwnership(Clipboard aClipboard, Transferable aContents){
        //do nothing
    }

    /**
     * Place a String on the clipboard, and make this class the
     * owner of the Clipboard's contents.
     * @param aString
     */
    public void setData(String aString){
        StringSelection stringSelection = new StringSelection(aString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    /**
     * Get the String residing on the clipboard.
     *
     * @return any text found on the Clipboard; if none found, return an
     * empty String.
     */
    public String getData() {
        String result           = "";
        Transferable contents   = null;

        Boolean retry;
        Integer tries = 0;
        do {
            try {
                retry     = false;
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                contents            = clipboard.getContents(null);
            } catch (Exception e) {
                log.debug ("Can't get a handle on Clipboard - wait 5 milis and try again (try="+ tries +").", e);
                retry = true;
                try {
                    Thread.sleep (5);
                } catch (InterruptedException e1) {}
            }

            tries += 1;
        } while (retry && tries < 3);

        Boolean hasTransferableText =  (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String)contents.getTransferData(DataFlavor.stringFlavor);
            }
            catch (UnsupportedFlavorException | IOException ex) {
                log.error ("Can't retrieve the clipboard contents.", ex);
            }
        }

        return result;
    }

}

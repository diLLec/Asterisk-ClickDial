package de.neue_phase.asterisk.ClickDial.jobs;


import com.google.common.eventbus.EventBus;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.constants.JobConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.SetWorkstateEvent;
import de.neue_phase.asterisk.ClickDial.settings.AutoConfig;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public class ScreenLockWatcherJob extends TimerTask implements IJob {
    private final ControllerConstants.JobTypes type = ControllerConstants.JobTypes.ScreenLockWatcherJob;
    private AtomicBoolean lockState                 = new AtomicBoolean (false);
    protected final Logger log 	                    = Logger.getLogger(this.getClass());
    private Timer timer                             = null;
    private Date lastrun                            = null;
    SettingsHolder settingsHolder                   = null;

    public ScreenLockWatcherJob () {

    }

    @Override
    public void startUp () throws InitException {
        // initial run to check if the AutoConfig webservice call is OK
        // also to ensure, that the configuration is in place before it is used
        this.log.debug ("Schedule for ScreenLock");
        this.timer = new Timer ();
        this.timer.scheduleAtFixedRate (this,
                                        0,
                                        JobConstants.ScreenLockWatcherJobInterval +
                                                new Random().nextInt (JobConstants.ScreenLockWatcherIntervalVariance));
    }

    @Override
    public ControllerConstants.JobTypes getName () {
        return this.type;
    }

    /**
     * Timer to renew AutoConfig data
     */
    @Override
    public void run () {
        lastrun = new Date();

        this.log.debug ("Checking for ScreenLock");
        try {
            boolean newState = getScreenStatus ();
            if (lockState.get () != newState) {
                // TODO: make target workstate configurable
                if (newState) // locked
                    EventBusFactory.getThreadPerTaskEventBus ().post (new SetWorkstateEvent (InterfaceConstants.WorkstateTypes.AusserHaus));
                else // not locked
                    EventBusFactory.getThreadPerTaskEventBus ().post (new SetWorkstateEvent (InterfaceConstants.WorkstateTypes.Arbeit));

                lockState.set (newState);
            }

        } catch (Exception e) {
            log.error (e);
        }
    }

    /**
     * Shutdown the AutoConfigJob
     */
    @Override
    public void shutdown () {
        timer.cancel ();
    }

    /**
     *
     * @return yes/no
     */
    public boolean isAlive () {
        long diff = new Date().getTime () - this.lastrun.getTime ();
        return (TimeUnit.MILLISECONDS.toSeconds (diff) > JobConstants.AutoConfigJobInterval * JobConstants.JobMaxSlippedIntervalTimeMultiplier);
    }

    /**
     *
     * @return true if workstation screen is locked
     * @throws Exception
     */
    public boolean getScreenStatus() throws Exception {
        int DESKTOP_SWITCHDESKTOP = 256;
        int hwnd = -1;
        int ret = -1;

        hwnd = User32.INSTANCE.OpenDesktopW("Default".toCharArray(), 0, 0, DESKTOP_SWITCHDESKTOP);

        if (hwnd != 0) {
            ret = User32.INSTANCE.SwitchDesktop(hwnd);
            User32.INSTANCE.CloseDesktop(hwnd);

            if (ret == 0) {
                log.debug("desktop locked");
                return true;
            } else {
                log.debug("desktop active");
                return false;
            }
        } else {
            throw new Exception ("ADA_Screenlock: could not get Desktop object");
        }
    }

}

/*
 * lean wrapper around the native WINAPI functions
 */
interface User32 extends StdCallLibrary {

    User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

    /*
     * HDESK WINAPI OpenDesktop(
     * __in  LPTSTR lpszDesktop,
     * __in  DWORD dwFlags,
     * __in  BOOL fInherit,
     * __in  ACCESS_MASK dwDesiredAccess
     * );
     */
    int OpenDesktopW(
            char[] lpszDesktop,
            int dwFlags,
            int fInherit,
            int dwDesiredAccess
    );

    /*
     * BOOL WINAPI CloseDesktop(
     * __in  HDESK hDesktop
     * );
     */
    int CloseDesktop(
            int hDesktop
    );

    /*
     * BOOL WINAPI SwitchDesktop(
     * __in  HDESK hDesktop
     * );
     */
    int SwitchDesktop(
            int hDesktop
    );
}
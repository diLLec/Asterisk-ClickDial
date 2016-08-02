package de.neue_phase.asterisk.ClickDial.jobs;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.neue_phase.asterisk.ClickDial.constants.ControllerConstants;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.constants.JobConstants;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;
import de.neue_phase.asterisk.ClickDial.controller.exception.InitException;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.GetWorkstateEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.SetWorkstateEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.UpdateWorkstateEvent;
import de.neue_phase.asterisk.ClickDial.settings.AutoConfig;
import de.neue_phase.asterisk.ClickDial.settings.SettingsElement;
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
    private SettingsHolder settingsHolder                   = null;
    private InterfaceConstants.WorkstateTypes initialState  = null;

    public ScreenLockWatcherJob () {

    }

    @Override
    public void startUp () throws InitException {
        // initial run to check if the AutoConfig webservice call is OK
        // also to ensure, that the configuration is in place before it is used
        GetWorkstateEvent event = new GetWorkstateEvent ();
        EventBusFactory.getThreadPerTaskEventBus ().post (event);
        event.getReponse (3000);
        this.initialState = event.getCurrentWorkstate ();

        this.log.debug ("Schedule for ScreenLock");
        this.timer = new Timer ();
        this.timer.scheduleAtFixedRate (this,
                                        0,
                                        JobConstants.ScreenLockWatcherJobInterval +
                                                new Random().nextInt (JobConstants.ScreenLockWatcherIntervalVariance));
    }

    /**
     * Update the initial state if someone updates the state in or outside the application
     * @param event The update event with the new workstate
     */
    @Subscribe
    public void onUpdateWorkstateEvent (UpdateWorkstateEvent event) {  // from Display UI thread
        this.initialState = event.getTargetWorkstate ();
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
        boolean newState = getScreenStatus ();

        if (lockState.get () != newState) {
            SettingsElement enabled = SettingsHolder.getInstance ().get (SettingsConstants.SettingsTypes.global)
                                                                   .get ("change_workstate_on_screenlock");
            if (enabled.getValue ().equals ("1")) {
                SettingsElement targetWorkstate = SettingsHolder.getInstance ().get (SettingsConstants.SettingsTypes.global)
                                                                               .get ("change_workstate_on_screenlock_target_workstate");

                InterfaceConstants.WorkstateTypes targetWorkstateEnum;
                try {
                    targetWorkstateEnum = InterfaceConstants.WorkstateTypes.valueOf (targetWorkstate.getValue ());
                }
                catch (IllegalArgumentException e) {
                    log.error ("Configuration on change_workstate_on_screenlock_target_workstate does not fit to the constants in InterfaceConstants.WorkstateTypes ");
                    targetWorkstateEnum = InterfaceConstants.WorkstateTypes.AusserHaus;
                }
                log.debug ("Screen lock state change");
                log.debug (String.format ("Screen lock state change to '%b' (locked state: '%s' | not-locked state: '%s')",
                                          newState,
                                          targetWorkstateEnum.toString (),
                                          this.initialState.toString ()));
                if (newState) {
                    // locked
                    EventBusFactory.getThreadPerTaskEventBus ().post (new SetWorkstateEvent (targetWorkstateEnum)); // will also update this.initialState through event
                }
                else {
                    // not locked
                    EventBusFactory.getThreadPerTaskEventBus ().post (new SetWorkstateEvent (this.initialState)); // will also update this.initialState through event
                }
            }
            lockState.set (newState);
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
     */
    private boolean getScreenStatus()  {
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
            log.error ("ADA_Screenlock: could not get Desktop object");
            return this.lockState.get ();
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
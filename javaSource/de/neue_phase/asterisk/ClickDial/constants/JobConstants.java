package de.neue_phase.asterisk.ClickDial.constants;

public class JobConstants {

    /** this multiplier is used to multiply the interval time of a job. The result will then be used to determine
     *  if the timer/job is dead **/
    public static final double JobMaxSlippedIntervalTimeMultiplier = 1.5;

    /** The interval in which the AutoConfigJob will check for new AutoConfig data */
    public static final Integer	AutoConfigJobInterval = 600000; // 5 minutes

    /** The variance which will be added to the Interval above to prevent peaks on the Webservice */
    public static final Integer	AutoConfigJobIntervalVariance = 5000; // 5 seconds variant

    /** The interval in which the WorkstateGetter will check for new Workstate data */
    public static final Integer	WorkstateGetterJobInterval = 50000; // 5 seconds // TODO: set to 5000 milis

    /** The variance which will be added to the Interval above to prevent peaks on the Webservice */
    public static final Integer	WorkstateGetterIntervalVariance = 1000; // 1 seconds variant

    /** The variance which will be added to the Interval above to prevent peaks on the Webservice */
    public static final Integer	ScreenLockWatcherIntervalVariance = 50; // 50 millis

    /** The variance which will be added to the Interval above to prevent peaks on the Webservice */
    public static final Integer	ScreenLockWatcherJobInterval = 50000; // 5 secs // TODO: set to 500 milis
}

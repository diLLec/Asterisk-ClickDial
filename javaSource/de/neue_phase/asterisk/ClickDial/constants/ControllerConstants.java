package de.neue_phase.asterisk.ClickDial.constants;
/**
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 * @since 03. Juni 2007
 * constants defined to be used by controller package
 * 
 */
public class ControllerConstants {

	public enum ControllerTypes {
		none,
		Base,
		DialWindow,
		Settings,
		DataSource,
		CallWindow,
		TrayIcon
	}

	public enum ServiceInterfaceTypes {
		Webservice,
		AsteriskManagerInterface
	}

	public enum ServiceInterfaceProblems {
		ConnectionProblem,
		AuthenticationDataExpired
	}

	public enum JobTypes {
		AutoConfig
	}
	/**
	 * @see de.neue_phase.asterisk.ClickDial.controller.DialWindowController
	 */
	public final static int AsteriskController_MaxSettingsDialogFail = 5;
	
	/**
	 * @see de.neue_phase.asterisk.ClickDial.serviceInterfaces.AsteriskManagerInterface
	 */
	public final static String AsteriskController_OriginateContext = "test";

	public final static Integer MaxJobRestartCount = 10;
	public final static Integer JobWatchdogInterval = 5000; // milis
}

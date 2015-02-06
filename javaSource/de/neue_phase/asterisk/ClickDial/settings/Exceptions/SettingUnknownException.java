/**
 * 
 */
package de.neue_phase.asterisk.ClickDial.settings.Exceptions;

/**
 * thrown if a setting can't be interpreted
 *
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SettingUnknownException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1603972055416559029L;

	/**
	 * 
	 */
	public SettingUnknownException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public SettingUnknownException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public SettingUnknownException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SettingUnknownException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}

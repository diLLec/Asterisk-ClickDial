package de.neue_phase.asterisk.ClickDial.settings.Exceptions;

/**
 * thrown if a settings file can't be read
 *
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SettingsFileCorruptException extends Exception {

	private static final long serialVersionUID = -4428386196728714913L;

	public SettingsFileCorruptException() {
		// TODO Auto-generated constructor stub
	}

	public SettingsFileCorruptException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public SettingsFileCorruptException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public SettingsFileCorruptException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}

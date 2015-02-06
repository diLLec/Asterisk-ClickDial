/**
 * 
 */
package de.neue_phase.asterisk.ClickDial.controller.exception;

/**
 * InitException will be thrown from any controller that has
 * detected an failure on startUp()
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */
public class InitException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public InitException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public InitException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public InitException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public InitException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}

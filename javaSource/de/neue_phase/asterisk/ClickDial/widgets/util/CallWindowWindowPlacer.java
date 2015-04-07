package de.neue_phase.asterisk.ClickDial.widgets.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants.CallWindowAppearEdges;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants.SettingsTypes;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;

/**
 * helper class to keep track of the CallWindow positions
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class CallWindowWindowPlacer {

	CallWindowAppearEdges edge 	= CallWindowAppearEdges.right;
	Integer				  index	= 1;
	Integer				  mon   = SettingsHolder.getInstance().get(SettingsTypes.global).getValueInteger("call_window_monitor");
	Rectangle			  mb	= null;
	
	public CallWindowWindowPlacer(CallWindowAppearEdges edge, int Iindex) {
		this.edge 	= edge;
		index	  	= Iindex;
		mb			= Display.getCurrent().getMonitors()[mon - 1].getBounds();
	}
	
	public Point getLocation () {
		Point p = calcDefault();

		if (edge == CallWindowAppearEdges.upper) 
			p =  calcUpper();

		if (edge == CallWindowAppearEdges.lower) 
			p = calcLower();
		
		if (edge == CallWindowAppearEdges.left) 
			p = calcLeft();
		
		if (edge == CallWindowAppearEdges.right) 
			p = calcRight();
		
		System.out.println("CallWindowWindowPlacer.getLocation: " +p);
		
		return p;
	}
	/**
	 * returns the direction, even -> window on the right (increment); 
	 *                        not even -> window on the left (decrement)
	 * @return
	 */
	private int giveDirectionByIndex () {
		if (index % 2 == 0)
			return 1;
		else
			return -1;
	}
	
	private int giveIndexValue () {
		return (int) index/2;
	}
	
	private Point calcUpper () {
		return new Point (
		// -- draw on x + width/2 to be on the middle, decrement with window width / 2 -> middle
							mb.x + mb.width / 2 - InterfaceConstants.CallWindow_size.x / 2
							+ ( giveDirectionByIndex () * (giveIndexValue() * (InterfaceConstants.CallWindow_size.x + 5))),
		// -- we draw from top to down - no windowH needed 
							mb.y + 5
						);
	}

	private Point calcLower () {
		return new Point (
		// -- same as upper edge
				mb.x + mb.width / 2 - InterfaceConstants.CallWindow_size.x / 2
				+ ( giveDirectionByIndex () * (giveIndexValue() * (InterfaceConstants.CallWindow_size.x + 5))),
		// -- scroll to the half of the window, decrement the window size,
		// -- draw from there the full window in the y  middle
				mb.y + mb.height - 5 - InterfaceConstants.CallWindow_size.y
			);		
	}

	private Point calcLeft () {
		return new Point (
		// -- we draw from left to right -> add the gap only
				mb.x + 5,
		// -- scroll to the half of the window, decrement the window size,
		// -- draw from there the full window in the y  middle
				mb.y + mb.height / 2 - InterfaceConstants.CallWindow_size.y / 2
				+ ( giveDirectionByIndex () * (giveIndexValue() * (InterfaceConstants.CallWindow_size.y + 5)))
			);
	}

	private Point calcRight () {
		return new Point (
		// -- scroll to the rigth edge, decrement gap, decrement FULL window width -> we draw from left to right!
				mb.x + mb.width - 5 - InterfaceConstants.CallWindow_size.x,
		// -- same as left edge
				mb.y + mb.height / 2 - InterfaceConstants.CallWindow_size.y / 2
				+ ( giveDirectionByIndex () * (giveIndexValue() * (InterfaceConstants.CallWindow_size.y + 5)))
			);	
	}

	private Point calcDefault () {
		return calcRight();
	}
	
	/**
	 * get window index
	 * @return window index
	 */
	public Integer getIndex () {
		return index;
	}
}

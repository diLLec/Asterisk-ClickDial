package de.neue_phase.asterisk.ClickDial.settings;


import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Combo;

/**
 * hold the form Controls used in the SettingsWindow 
 * to be referenced by SettingsElement to dispose/hide them
 * if another settings view is switched on or the window is 
 * closed and every resource must be saved.
 *  
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SettingsViewReferencer {


	private final ArrayList<Control> 	elements 		= new ArrayList<Control>();
	private Control 				 	settingObject 	= null;
	
	public SettingsViewReferencer() {}
	
	/**
	 * constructor
	 * @param settingHolder the SWT.Control object which will HOLD the setting
	 */
	public void setSettingHolderValue (Control settingHolder) {
		this.settingObject = settingHolder;
	}
	
	/** 
	 * add an view element
	 * @param c the SWT.Control implementing view element
	 */
	public void addElement (Control c) {
		elements.add(c);
	}
	
	/**
	 * look if any views have been disposed. if any
	 * has been - dispose all other referencing fields
	 * @return if some view has been disposed
	 */
	public boolean disposed () {
		Iterator<Control> i = elements.iterator();
		Control 		cur;
		boolean disposed 	= false;

		while (i.hasNext()) {
			cur = i.next();
			
			if (cur.isDisposed()) 
				disposed = true;
			else {
				/* -- all element should be disposed if one is
				 * 	  if this is false, we are here and disposing
				 * 	  those dirty widgets by hand.
				 *    
				*/
				if (disposed)
					cur.dispose();
			}
		}
		return disposed;
	}
	
	/**
	 * called to hide a view
	 */
	public void hide () {
		toggleHideShow(false);
	}

	/**
	 * called to show a view
	 */
	public void show () {
		toggleHideShow(true);
	}
	
	/**
	 * set every associated settings element visible
	 * @param show true => show  // false => hide
	 */
	private void toggleHideShow (boolean show)  {
		Iterator<Control> i = elements.iterator();
		while (i.hasNext()) {
			
			Control cur = (Control) i.next();
			cur.setVisible(show);
			((GridData) cur.getLayoutData()).exclude = ! show;
		}		
	}
	
	/**
	 * get the String representation of the settingHolder object
	 * @return the String representation of the settingHolder object
	 */
	public String getConfigurationString () {
		System.out.println("getConfigurationString: "+ settingObject.getClass().toString());
		
		String re = null;
		
		if (settingObject instanceof Button)
			re = (((Button) settingObject).getSelection()) ? "1" : "0";
		else if (settingObject instanceof Text)
			re = ((Text) settingObject).getText();
		else if (settingObject instanceof Spinner)
			re = new Integer( ((Spinner) settingObject).getSelection() ).toString();
		else if (settingObject instanceof Combo)
			re = ((Combo) settingObject).getItem(((Combo) settingObject).getSelectionIndex());
		else
			System.out.println("getConfigurationString: unknown");

		return new String (re);
		
	}
	
	
}

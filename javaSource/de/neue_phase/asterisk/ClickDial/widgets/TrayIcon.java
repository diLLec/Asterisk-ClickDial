package de.neue_phase.asterisk.ClickDial.widgets;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.boot.Bootstrap;
import de.neue_phase.asterisk.ClickDial.controller.TrayIconController;

/**
 * TrayIcon - what should I say ?!
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */


public class TrayIcon {

	private final Display     displayRef 	= Display.getCurrent();
	private Tray		tray				= displayRef.getSystemTray();
	private TrayItem	trayItem			= null;
	private TrayIconController tictrl		= null;
	private Menu menu						= null;
	private String[] menuItems				= null;
	
	
	/**
	 * constructor
	 * @param ctrl
	 * @param menuItems
	 */
	public TrayIcon(TrayIconController ctrl, String[] menuItems) {
		tictrl 			= ctrl;
		this.menuItems 	= menuItems; 
		initTrayItem();
		initTrayMenu ();
		
		trayItem.addMenuDetectListener(ctrl);
		
	}
	
	/**
	 * give the icon a menu
	 */
	private void initTrayMenu () {
		menu = new Menu (Bootstrap.primaryShell, SWT.POP_UP);
		MenuItem item;

		for (String itemText : menuItems) {
			
			if ( itemText == null )
				continue;
			
			item = new MenuItem(menu, SWT.PUSH);

			item.setText(itemText);
			item.addSelectionListener(tictrl);
		}
	}
	
	/**
	 * initialize the tray icon 
	 */
	private void initTrayItem () {
		if (  !  isTrayValid() )
			return; /* bail out */

		trayItem =  new TrayItem (tray, SWT.NONE);
		trayItem.addSelectionListener(tictrl);
		
		File f = new File( InterfaceConstants.TrayIcon_Icon );
		if (f.exists()) 
			trayItem.setImage(new Image(displayRef, f.getPath()));
		trayItem.setToolTipText(InterfaceConstants.myName);
	}
	
	/**
	 * check if the trayicon can be used
	 * @return true if tray can be used, false if not
	 */
	public boolean isTrayValid () {
		return (tray != null);
	}
	
	/**
	 * show the menu on MenuDetectedListener
	 */
	public void showMenu () {
		if (  !  isTrayValid() )
			return; /* bail out */
		
		menu.setVisible(true);
	}
	
	/**
	 * dispose function
	 */
	public void dispose() {
	}
	
	
}

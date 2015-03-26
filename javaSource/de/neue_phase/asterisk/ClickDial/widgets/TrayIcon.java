package de.neue_phase.asterisk.ClickDial.widgets;

import java.io.File;
import java.util.HashMap;

import com.google.common.eventbus.Subscribe;
import de.neue_phase.asterisk.ClickDial.eventbus.events.ManagerProblemEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.ManagerProblemResolveEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants.WorkstateTypes;
import de.neue_phase.asterisk.ClickDial.boot.Bootstrap;
import de.neue_phase.asterisk.ClickDial.controller.TrayIconController;

import javax.swing.*;

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
    private ToolTip currentTip              = null;

    private MenuItem managerConnectionState     = null;
    private MenuItem webserviceConnectionState  = null;
    private MenuItem workstate                  = null;
    public static enum LampStyle {
        GREEN,
        RED
    }
    private HashMap<LampStyle, Image>  lampImages = new HashMap<> ();
    private HashMap<WorkstateTypes, Image>  workstateImages = new HashMap<> ();
    private HashMap<WorkstateTypes, Image>  workstateTrayIcons = new HashMap<> ();

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

        initLampsInMenu();
        initLWorkstateInMenu ();

        new MenuItem(menu, SWT.SEPARATOR);

		for (String itemText : menuItems) {
			
			if ( itemText == null )
				continue;
			
			item = new MenuItem(menu, SWT.PUSH);

			item.setText(itemText);
			item.addSelectionListener(tictrl);
		}
	}

    /**
     * init the lamps in the tray
     */
    private void initLampsInMenu () {
        managerConnectionState      = new MenuItem(menu, SWT.PUSH);
        webserviceConnectionState   = new MenuItem(menu, SWT.PUSH);

        File f;

        f = new File(InterfaceConstants.SettingsTypeIcons_Path + "green_lamp" +
                             InterfaceConstants.SettingsTypeIcons_Suffix);

        lampImages.put(LampStyle.GREEN, new Image(displayRef, f.getPath()));

        f = new File(InterfaceConstants.SettingsTypeIcons_Path + "red_lamp" +
                             InterfaceConstants.SettingsTypeIcons_Suffix);
        lampImages.put(LampStyle.RED, new Image(displayRef, f.getPath()));

        managerConnectionState.setText ("Manager Status");
        managerConnectionState.setImage (lampImages.get (LampStyle.GREEN));

        webserviceConnectionState.setText ("Webservice Status");
        webserviceConnectionState.setImage (lampImages.get (LampStyle.GREEN));
    }

    /**
     * init workstate items in menu
     */
    private void initLWorkstateInMenu () {
        workstate      = new MenuItem(menu, SWT.CASCADE);
        File f;

        f = new File(InterfaceConstants.SettingsTypeIcons_Path + "status_arbeit" +
                             InterfaceConstants.SettingsTypeIcons_Suffix);
        workstateImages.put(WorkstateTypes.Arbeit, new Image(displayRef, f.getPath()));

        f = new File(InterfaceConstants.SettingsTypeIcons_Path + "status_ausserhaus" +
                             InterfaceConstants.SettingsTypeIcons_Suffix);
        workstateImages.put(WorkstateTypes.AusserHaus, new Image(displayRef, f.getPath()));

        f = new File(InterfaceConstants.SettingsTypeIcons_Path + "status_feierabend" +
                             InterfaceConstants.SettingsTypeIcons_Suffix);
        workstateImages.put(WorkstateTypes.Feierabend, new Image(displayRef, f.getPath()));

        f = new File(InterfaceConstants.SettingsTypeIcons_Path + "status_pause" +
                             InterfaceConstants.SettingsTypeIcons_Suffix);
        workstateImages.put(WorkstateTypes.Pause, new Image(displayRef, f.getPath()));

        workstate.setText ("Arbeitsstatus");
        Menu submenu    = new Menu (Bootstrap.primaryShell, SWT.DROP_DOWN);
        MenuItem sub    = new MenuItem(submenu, SWT.PUSH);
        sub.setText (WorkstateTypes.Arbeit.toString ());
        sub.setImage (workstateImages.get (WorkstateTypes.Arbeit));
        sub.addSelectionListener(tictrl);

        sub    = new MenuItem(submenu, SWT.PUSH);
        sub.setText (WorkstateTypes.AusserHaus.toString ());
        sub.setImage (workstateImages.get (WorkstateTypes.AusserHaus));
        sub.addSelectionListener(tictrl);

        sub    = new MenuItem(submenu, SWT.PUSH);
        sub.setText (WorkstateTypes.Feierabend.toString ());
        sub.setImage (workstateImages.get (WorkstateTypes.Feierabend));
        sub.addSelectionListener(tictrl);

        sub    = new MenuItem(submenu, SWT.PUSH);
        sub.setText (WorkstateTypes.Pause.toString ());
        sub.setImage (workstateImages.get (WorkstateTypes.Pause));
        sub.addSelectionListener(tictrl);

        workstate.setMenu (submenu);
    }



    @Subscribe public void updateManagerLamp (ManagerProblemEvent event) {
        managerConnectionState.setImage (lampImages.get (LampStyle.RED));
    }

    @Subscribe public void updateManagerLamp (ManagerProblemResolveEvent event) {
        managerConnectionState.setImage (lampImages.get (LampStyle.GREEN));
    }

	/**
	 * initialize the tray icon 
	 */
	private void initTrayItem () {
		if (  !  isTrayValid() )
			return; /* bail out */

		trayItem =  new TrayItem (tray, SWT.NONE);
		trayItem.addSelectionListener(tictrl);

        File f;

        f = new File(InterfaceConstants.TrayIcon_Path + "status_arbeit.ico");
        workstateTrayIcons.put(WorkstateTypes.Arbeit, new Image(displayRef, f.getPath()));

        f = new File(InterfaceConstants.TrayIcon_Path + "status_ausserhaus.ico");
        workstateTrayIcons.put(WorkstateTypes.AusserHaus, new Image(displayRef, f.getPath()));

        f = new File(InterfaceConstants.TrayIcon_Path + "status_feierabend.ico");
        workstateTrayIcons.put(WorkstateTypes.Feierabend, new Image(displayRef, f.getPath()));

        f = new File(InterfaceConstants.TrayIcon_Path + "status_pause.ico");
        workstateTrayIcons.put(WorkstateTypes.Pause, new Image(displayRef, f.getPath()));

        trayItem.setImage (workstateTrayIcons.get (WorkstateTypes.Arbeit));
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
        tray.dispose ();
	}


    /**
     * popup a message above the tray
     * @param message
     * @param type
     */
    public void popupMessage (String message, int type) {

        if (currentTip != null)
            currentTip.dispose ();

        ToolTip currentTip = new ToolTip (Bootstrap.primaryShell, type);
        currentTip.setMessage (message);
        currentTip.setAutoHide (true);
        trayItem.setToolTip (currentTip);
    }

	public void updateTrayIconByWorkstate (WorkstateTypes targetWorkstate) {
        if (workstateTrayIcons.containsKey (targetWorkstate))
            trayItem.setImage (workstateTrayIcons.get (targetWorkstate));
    }

}

package de.neue_phase.asterisk.ClickDial.widgets;

import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map.Entry;

import de.neue_phase.asterisk.ClickDial.controller.BaseController;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


import de.neue_phase.asterisk.ClickDial.constants.InterfaceConstants;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants.SettingsElementType;
import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants.SettingsTypes;
import de.neue_phase.asterisk.ClickDial.boot.Bootstrap;
import de.neue_phase.asterisk.ClickDial.settings.SettingsAbstractMaster;
import de.neue_phase.asterisk.ClickDial.settings.SettingsElement;
import de.neue_phase.asterisk.ClickDial.settings.SettingsHolder;
import de.neue_phase.asterisk.ClickDial.settings.SettingsViewReferencer;

/**
 * The SettingsWindow
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SettingsWindow implements Listener {

	private Shell 		shell 			= null;
	private Composite   compo 			= null;
	private Display 	displayRef 		= null;
	private SettingsHolder settingsRef 	= null;
	protected final Logger log 			= Logger.getLogger(this.getClass());

	private int		returnCode			= 0;
	
	private SettingsTypes currentView	= SettingsTypes.global;

	private TreeItem lastEventButton	= null;
	private Tree	 tree				= null;

	public SettingsWindow(SettingsHolder settings) {
		this.displayRef 	= Display.getDefault ();
		this.settingsRef 	= settings;
	}
	
	/**
	 * open the window with the standard view
	 * @return error_code of the SettingsWindow (clears if saving was ok)
	 */
	public int open() {
		return open (currentView);
	}
	
	/**
	 * initialize the shell and bring up the components 
	 * @param type the view which will first be shown
	 * @return error_code of the SettingsWindow (clears if saving was ok)
	 */
	public int open( SettingsConstants.SettingsTypes type ) {
		shell = new Shell (BaseController.getInstance ().getPrimaryShell (), SWT.APPLICATION_MODAL | SWT.TITLE | SWT.CLOSE );
		shell.addListener(SWT.Close, SettingsWindow.this);
		shell.setText("Asterisk ClickDial Settings Window");

        shell.setSize(	SettingsConstants.SettingsWindow_width, 
  			  			SettingsConstants.SettingsWindow_heigth
  			  		  );

        GridLayout grid = new GridLayout(2, false);
        shell.setLayout(grid);

        // -- build the upper menu
		GridData treeLayoutData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true);
		treeLayoutData.widthHint 	= (int) (SettingsConstants.SettingsWindow_width * 0.15);
		treeLayoutData.heightHint 	= SettingsConstants.SettingsWindow_heigth;
        buildMenu(treeLayoutData);

		// -- create the composite area, where all forms will be placed on  
        compo = new Composite(shell, SWT.NORMAL);

        GridData compoGridData 		= new GridData(SWT.END, SWT.BEGINNING, true, true);
		compoGridData.heightHint 	= SettingsConstants.SettingsWindow_heigth;
		compoGridData.widthHint 	= (int) (SettingsConstants.SettingsWindow_width * 0.85);
        compo.setLayoutData(compoGridData);
		log.debug ("Composite has the following layout widthHint: " + ((GridData) compo.getLayoutData ()).widthHint);

		GridLayout formLayout = new GridLayout (2, false);
        compo.setLayout(formLayout);
        compo.setBackground(new Color(displayRef, 255, 255, 255));

		// -- bring up the standard view
		showView(type);

        // make this thing moveable! 
        Listener l = new Listener() {
        	int startX, startY;
            public void handleEvent(Event e)  {
				if (e.type == SWT.MouseDown && e.button == 1) {
					startX = e.x;
					startY = e.y; 
				}
				if (e.type == SWT.MouseMove && (e.stateMask & SWT.BUTTON1) != 0) {
					Point p = shell.toDisplay(e.x, e.y);
					p.x -= startX;
					p.y -= startY;
					shell.setLocation(p);
				}
            }
        };
        
        shell.addListener(SWT.MouseDown, l);
        shell.addListener(SWT.MouseMove, l);
        shell.addListener(SWT.Paint, l);

        /* locate this element in the middle of the primary screen  */
		Rectangle shellSize 	= shell.getBounds();
		Rectangle r 			= Bootstrap.priMonSize;
		shell.setLocation(  (r.width - shellSize.width) / 2 , (r.height - shellSize.height) / 2);

		shell.open();
		while (!shell.isDisposed ()) {
			if (!displayRef.readAndDispatch ()) displayRef.sleep ();
		}
		
		return this.returnCode;
	}

	/**
	 * build the tree menu
	 */
	private void buildMenu (GridData treeLayoutData) {
		// -- firstly create a tree
		log.debug ("building menu");
		this.tree 	= new Tree (shell, SWT.SINGLE );
		
		// -- then set the layout data to place it
		tree.setLayoutData(treeLayoutData);

		// -- get all registered expanders
		Iterator<Entry<SettingsConstants.SettingsExpander, ArrayList<SettingsAbstractMaster>>> i  = settingsRef.getRegisteredExpanders();		
		Entry<SettingsConstants.SettingsExpander, ArrayList<SettingsAbstractMaster>> entry;

		Iterator<SettingsAbstractMaster> settingsTile;
		TreeItem expander , item;
		
		File f;
		
		/* add the image if it exists */
		File arrow 		= new File( InterfaceConstants.SettingsTreeIconExpander );
		Image arrowImg  = null;
		if (arrow.exists())
			arrowImg = new Image(displayRef, arrow.getPath());

		String name;
		while (i.hasNext()) 
		{
			entry = i.next();

			// -- we create the 'uber' item
			expander = new TreeItem (tree, SWT.NORMAL);
			expander.setText(entry.getKey().toString());
			
			// -- add the arrow image 
			if (arrowImg != null)
				expander.setImage(arrowImg);

			// -- we create all "sub-items"
			settingsTile 	= entry.getValue().iterator();
			
			while (settingsTile.hasNext()) {
				item = new TreeItem (expander, SWT.NORMAL);
				name = settingsTile.next().getType().toString();
				item.setText(name);

				/* add the image if it exists */
				f = new File( InterfaceConstants.SettingsTreeIconPath + 
			    			  name + 
			    			  InterfaceConstants.SettingsTypeIcons_Suffix);
				if (f.exists()) 
					item.setImage(new Image(displayRef, f.getPath()));
				
				// -- mark the File obj behind f to be gc'ed
				f = null;
			}
		}

		tree.addListener(SWT.MouseDown, this);
	}
	
	/** 
	 * handle events coming from the menu
	 * @param e the event which has occurred 
	 */
	
	public void handleEvent(Event e) {

		log.debug ("got Event (SettingsWindow): '"+e.toString()+"'");
		if (e.type == SWT.Close) {
			closeMe(0);
			return;
		}
		
		if (e.type == SWT.MouseDown) 
		{
			TreeItem eventItem = tree.getItem(new Point (e.x, e.y));
			if (eventItem == null)
				return;

			String buttonText = eventItem.getText();
			
			/* toggle on for the current, toggle off for the last event button */			
			if (lastEventButton == eventItem)
				return;
			
			/* save this button, to be able to toggle it off */
			lastEventButton = eventItem;
			
			/* iterate through registered types and find the field definitions */
			Iterator <SettingsConstants.SettingsTypes> i  = settingsRef.getRegisteredTypes();
			while (i.hasNext()) 
			{
				SettingsConstants.SettingsTypes cur = i.next();
				if (buttonText.equals(cur.toString())) {
					this.hideCurrentView();
					this.showView (cur);
				}
			}	
		}
	}
	
	/** 
	 * save current view
	 */
	
	private void saveAll () {
		log.debug ("Saving modified configuration data");
		settingsRef.saveSettings();
	}
	
	/**
	 * hide current settings view
	 *
	 */
	private void hideCurrentView () {
		log.debug ("Hiding view "+ this.currentView);
		
		Iterator <SettingsElement> i = settingsRef.getSettingsElements(this.currentView);
		SettingsElement element;
		while (i.hasNext()) 
		{
			element = i.next();
			if (element.isVisible() && element.getReferencingSettingsField() != null) 
			{
				element.getReferencingSettingsField().hide();
			}
		}
	}

	/**
	 * build the new elements out of field definitions
	 * @param type the new view which should be shown
     */
	private void showView ( SettingsConstants.SettingsTypes type) {
		
		log.debug ("Showing new view "+ type);
		
		Iterator <SettingsElement> i = settingsRef.getSettingsElements(type);
		SettingsElement element;
		
		/* setting the current view */
		this.currentView = type;

		while (i.hasNext()) 
		{
			element = i.next();
			if (element.isVisible()) 
			{
				if (element.isReferencingFieldUsable()) {
					// -- if the element got a referencing field - simply show it again
					element.getReferencingSettingsField().show();
					continue;
				}
				
				SettingsViewReferencer ref  = new SettingsViewReferencer();
				GridData data 				= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
				data.widthHint				= (int) (((GridData)compo.getLayoutData ()).widthHint * 0.50);
				//data.minimumWidth				= (int) (((GridData)compo.getLayoutData ()).widthHint * 0.50);

				Label l = new Label(compo, SWT.LEFT);
				l.setBackground(new Color(displayRef, 255,255,255));
				l.setText(element.getOfficialName());
				l.setLayoutData(data);
				ref.addElement(l);

				data 						= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
				data.widthHint				= (int) (((GridData)compo.getLayoutData ()).widthHint * 0.30);
				/*
				 * text buildings 
				 */
				if (element.getType().equals(SettingsElementType.text)) {
			
					Text t = new Text(compo, SWT.SINGLE | SWT.BORDER);
					t.setBackground(new Color(displayRef, 255,255,255));
					t.setLayoutData(data);
					log.debug ("width of text is: " + data.widthHint);

					if (element.getValue() != null) {
						t.setText(element.getValue());
					}

					if (element.isDisabled ())
						t.setEnabled (false);

					ref.addElement(t);
					ref.setSettingHolderValue(t);

				}
				if (element.getType().equals(SettingsElementType.checkbox)) {
					
					Button b = new Button(compo, SWT.CHECK);
					b.setBackground(new Color(displayRef, 255,255,255));
					b.setLayoutData(data);
					b.setAlignment(SWT.LEFT);
					if (element.getValue() != null) {
						b.setSelection(element.getValue().equals("1"));
					}

					if (element.isDisabled ())
						b.setEnabled (false);
					
					ref.addElement(b);
					ref.setSettingHolderValue( b );
				}
				
				if (element.getType().equals(SettingsElementType.dropdown)) {
					Combo combo = new Combo (compo, SWT.READ_ONLY);
					
					String[] dropdown_values 	= element.getSpecificSetting("dropdown_values");
					int 	 selected 			= 0;
					boolean  failure			= false;
					String	 selectedString		= element.getValue();

					if (dropdown_values != null) {
						/* find the selected one and find failures */
						for (int k = 0; k < dropdown_values.length; k++) {
							if (dropdown_values[k] == null) 
							{
								/* found a failure in that array which would crash the application - breakout */
								failure = true;
								break;
							}

							if (dropdown_values[k].equals(selectedString))
								selected = k;
						}
					
					}

					if (element.isDisabled ())
						combo.setEnabled (false);

					if ( ! failure ) {
						combo.setItems (element.getSpecificSetting("dropdown_values"));

						combo.pack();
						combo.setLayoutData(data);
						combo.select(selected);
						ref.addElement(combo);
						ref.setSettingHolderValue(combo);
					}
				}
				
				if (element.getType().equals(SettingsElementType.spinner)) {
					String[] min_max 	= element.getSpecificSetting("spinner_min_max");
					Spinner spinner		= new Spinner(compo, SWT.NORMAL);
					if (min_max.length == 2 && min_max[0] != null && min_max[1] != null)
					{
						spinner.setMinimum(new Integer (min_max[0]));
						spinner.setMaximum(new Integer (min_max[1]));
					}
					spinner.setSelection( new Integer(element.getValue()) );
					spinner.setLayoutData(data);

					if (element.isDisabled ())
						spinner.setEnabled (false);

					ref.addElement(spinner);
					ref.setSettingHolderValue(spinner);
				}
				
				element.setReferencingSettingsField(ref);
			}
			
		}
		/* ask the compo layout to lay out */
		compo.layout();
	}
	
	/** 
	 * the function which is called when we need to closedown
	 * @param returnCode The returncode which is set after the widget is closed down
	 */
	private void closeMe (int returnCode) {
		
		this.saveAll ();
		
		compo.dispose();
		shell.dispose();
		this.returnCode = returnCode;
	}
}

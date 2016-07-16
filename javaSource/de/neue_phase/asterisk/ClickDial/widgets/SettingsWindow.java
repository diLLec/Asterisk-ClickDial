package de.neue_phase.asterisk.ClickDial.widgets;

import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map.Entry;

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
	private Rectangle treeBounds		= null;
	private Rectangle shellBounds		= null;

	public SettingsWindow(Display displayRef, SettingsHolder settings) {
		this.displayRef 	= Display.getCurrent();
		this.settingsRef 	= settings;
		
		shell = new Shell (Bootstrap.primaryShell, SWT.APPLICATION_MODAL | SWT.TITLE | SWT.CLOSE );
		shell.addListener(SWT.Close, this);
		shell.setText("Asterisk ClickDial Settings Window");
	}
	
	/**
	 * reset the bounds of the compo, since this gets 
	 * modified if we do compo.pack()
	 *
	 */
	private void resetCompoBounds () {
		
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

        shell.setSize(	SettingsConstants.SettingsWindow_width, 
  			  			SettingsConstants.SettingsWindow_heigth
  			  		  );
        shellBounds = shell.getBounds();
        
        
        GridLayout gridShell = new GridLayout(2, false);
        shell.setLayout(gridShell);

        // -- build the upper menu 
        buildMenu(type);

		// -- create the composite area, where all forms will be placed on  
        compo = new Composite(shell, SWT.NORMAL);
        
        GridData compoGridData 		= new GridData(GridData.FILL_BOTH);
        compoGridData.widthHint 	= (shellBounds.width / 4) * 3 - 35;
        compoGridData.heightHint	= shellBounds.width - 50;
        compoGridData.verticalAlignment 	= GridData.BEGINNING;
        compoGridData.horizontalAlignment 	= GridData.BEGINNING;

        compo.setLayoutData(compoGridData);
        GridLayout gridCompo = new GridLayout(2, true);
        gridCompo.numColumns = 2;
        
        compo.setLayout(gridCompo);
        compo.setBackground(new Color(displayRef, 255, 255, 255));
        this.resetCompoBounds();

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
       
        // -- bring up the standard view 
        showView(type);
        
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
	 * @param type
	 */
	private void buildMenu (SettingsConstants.SettingsTypes type) {
		// -- firstly create a tree
		log.debug ("building menu");
		this.tree 	= new Tree (shell, SWT.SINGLE );
		
		// -- then set the layout data to place it
		GridData treeGridData 				= new GridData(SWT.FILL);
		treeGridData.verticalAlignment	 	= GridData.FILL;
		treeGridData.horizontalAlignment 	= GridData.FILL;
		treeGridData.widthHint 		= SettingsConstants.SettingsWindow_width / 4;
		treeGridData.heightHint 	= SettingsConstants.SettingsWindow_heigth - 100;
		
		tree.setSize(treeGridData.widthHint, treeGridData.heightHint);
		
		tree.setLayoutData(treeGridData);
		

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
		
		treeBounds 		 = tree.getBounds();
		treeBounds.width = treeGridData.widthHint;
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
	private void showView ( SettingsConstants.SettingsTypes type ) {
		
		log.debug ("Showing new view "+ type);
		
		Iterator <SettingsElement> i = settingsRef.getSettingsElements(type);
		SettingsElement element;
		int currentHeight = 50;
		
		
		/* setting the current view */
		this.currentView = type;

		while (i.hasNext()) 
		{
			element = i.next();
			log.debug("showView: Element " + element.getName());
			if (element.isVisible()) 
			{
				log.debug("showView 1 : Element " + element.getName());
				currentHeight += 20;

				if (element.isReferencingFieldUsable()) {
					// -- if the element got a referencing field - simply show it again
					log.debug("showView only show it : Element " + element.getName());
					element.getReferencingSettingsField().show();
					continue;
				}
				log.debug("showView create it : Element " + element.getName());
				//  -- if not - build it 
				
				SettingsViewReferencer ref  = new SettingsViewReferencer();
				GridData data 				= new GridData();
				data.exclude 				= false;
				data.horizontalAlignment    = GridData.BEGINNING;

				Label l = new Label(compo, SWT.LEFT);
				l.setBackground(new Color(displayRef, 255,255,255));
				l.setText(element.getOfficialName());
				l.setSize(80, 18);
				l.setLayoutData(data);
				ref.addElement(l);

				data 						= new GridData();
				data.exclude 				= false;
				data.horizontalAlignment    = GridData.END;
				data.widthHint				= 100;
				/**
				 * text buildings 
				 */
				if (element.getType().equals(SettingsElementType.text)) {
			
					Text t = new Text(compo, SWT.SINGLE | SWT.BORDER);
					t.setBackground(new Color(displayRef, 255,255,255));
					t.setSize(100, 18);
					t.setLayoutData(data);
					
					if (element.getValue() != null) {
						t.setText(element.getValue());
						log.debug("Setting Text: " +element.getValue());
					}

					if (element.isDisabled ())
						t.setEnabled (false);

					ref.addElement(t);
					ref.setSettingHolderValue(t);

				}
				if (element.getType().equals(SettingsElementType.checkbox)) {
					
					Button b = new Button(compo, SWT.CHECK);
					b.setBackground(new Color(displayRef, 255,255,255));
					b.setSize(100, 18);
					b.setLayoutData(data);
					b.setAlignment(SWT.LEFT);
					if (element.getValue() != null) {
						b.setSelection(element.getValue().equals("1"));
						log.debug("Button enabled?! : " + element.getValue());
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
						spinner.setMinimum(new Integer(min_max[0]).intValue());
						spinner.setMaximum(new Integer(min_max[1]).intValue());
					}
					spinner.setSelection( new Integer(element.getValue()) );
					spinner.setSize(100, 18);
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

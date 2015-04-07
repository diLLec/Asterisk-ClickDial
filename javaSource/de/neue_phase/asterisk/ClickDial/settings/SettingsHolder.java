/**
 * 
 */
package de.neue_phase.asterisk.ClickDial.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.neue_phase.asterisk.ClickDial.constants.*;

/**
 * The Settings holder holds Instances of Classes created from
 * SettingsAbstractMaster which hold SettingsElement and therefore
 * the Configuration data
 *  
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SettingsHolder {
	
	/* saving the relation between type <> SettingsAbstractMaster */
	
	private final HashMap<SettingsConstants.SettingsTypes, SettingsAbstractMaster> settings 
							= new HashMap<SettingsConstants.SettingsTypes, SettingsAbstractMaster>();
	
	/* saving the relation between ExpanderName <> SettingsAbstractMaster */
	
	private final HashMap<SettingsConstants.SettingsExpander, ArrayList<SettingsAbstractMaster>> settingsToExpander 
							= new HashMap<SettingsConstants.SettingsExpander, ArrayList<SettingsAbstractMaster>>();
	
	
	private final Logger log                = Logger.getLogger(this.getClass());
	private static SettingsHolder instance	= null;
	
	
	/**
	 * static function, which will return the only SettingsHolder in this Application
	 * @return singleton 
	 */
	public static SettingsHolder getInstance () {
		if (instance == null)
            instance = new SettingsHolder();
        
        return instance;
	}

    /**
     * constructor
     */
    private SettingsHolder()  {
        if (instance != null)
            throw new IllegalStateException("Already instantiated");
    }


    /**
	 * called if we need to register a new SettingsAbstractMaster into our
	 * settings HashMap
	 * @param setting
	 */
	
	private void registerSetting (SettingsAbstractMaster setting) 
	{
		log.info ("registerSetting: registered '" + setting.getType().toString() + "' Setting");
		
		
		
		/* fast indexing for SettingsType */ 
		settings.put(setting.getType()				, setting);
		
		/* fast indexing for SettingsExpander */
		SettingsConstants.SettingsExpander exp = setting.getExpander();
		log.debug("registerSetting: settings in '" + setting.getType().toString() + "' go to the '"+exp.toString()+"' expander");
		if (settingsToExpander.containsKey(exp))
			settingsToExpander.get(exp).add(setting);
		else {
			ArrayList<SettingsAbstractMaster> newArr = new ArrayList<SettingsAbstractMaster>();
			newArr.add(setting);
			settingsToExpander.put(exp, newArr);
		}
	}
	
	private SettingsAbstractMaster migrateData ( Class<?> c, SettingsAbstractMaster from ) {
		log.debug ("We need to migrate data!");

		SettingsAbstractMaster instance = null;
		try {
			instance = (SettingsAbstractMaster) c.newInstance();

			/* migrate fields */
			instance.migrateFrom(from);			
		}
		catch (Exception ex)  {
			log.error("Exception while running migration",ex);
		}			


		return (SettingsAbstractMaster) instance;		
	}
	
	/**
	 * newTile gets a classname. with it it does the following
	 * 	- look if there may be an existing configuration file in configSearchLocation
	 * 	- if not - create an instance of 'c' 
	 * @param c
	 * @return
	 */
	public boolean newTile (Class<?> c) {
		
		File potentionalSettingsFile = new File(SettingsConstants.configSearchLocation + 
												c.getSimpleName() +
												SettingsConstants.configSearchSuffix
												);
		
		if (potentionalSettingsFile.exists()) {
			
			/* settings file exists - load it */
			
			SettingsAbstractMaster obj; 
			log.debug("Reading " + potentionalSettingsFile.getAbsolutePath());
			try
			{
				FileInputStream fis 			= new FileInputStream(potentionalSettingsFile);
				ObjectInputStream in 			= new ObjectInputStream(fis);
				obj								= (SettingsAbstractMaster) in.readObject();
				if (obj.version < SettingsConstants.SettingsAbstractMaster_version) {
					// -- the loaded data is OLD! migrate data.
					log.info("You have an old version of "+c.getName()+" (your="+obj.version+" current="+SettingsConstants.SettingsAbstractMaster_version+"). I will migrate your data now");
					registerSetting(migrateData(c, obj));
					
					// -- mark the loaded object to be removed by next gc run
					obj = null;
				}
				else
					registerSetting (obj);

				in.close(); fis.close();
			} catch(Exception ex) {
				log.error ("Failed to read configuration file '" + potentionalSettingsFile.getAbsolutePath()+"'", ex);
			}
		}
		else {
			
			/* settings file is not existent - create an instance of the class */
			
			log.debug("Configuration File "+ potentionalSettingsFile.getAbsolutePath()+ " does not exist");
			Object instance = null;
			try {
				instance = c.newInstance();
				Method m = c.getMethod("getType");				
				registerSetting ( (SettingsAbstractMaster) instance);
				saveTile ((SettingsConstants.SettingsTypes) m.invoke(instance));
				
			}
			catch (Exception ex)  {
				log.error ("Failed to create configuration file '" + potentionalSettingsFile.getAbsolutePath()+"'", ex);
			}			
		}
		return true;
	}
	
	
	/**
	 * saveTile with type only
	 * @param type
	 */
	private void saveTile (SettingsConstants.SettingsTypes type) {
		saveTile(settings.get(type));
	}

	/**
	 * saveTile gets the instance to save in the appropriate .cfg file 
	 * in 'configSearchLocation'
	 * @param instance
	 */
	private void saveTile (SettingsAbstractMaster instance) {
		if (instance != null) {
			
			File potentionalSettingsFile = new File(SettingsConstants.configSearchLocation + 
													instance.getClass().getSimpleName() +
													SettingsConstants.configSearchSuffix
													);			
			
			log.debug("Saving tile '"+instance.getType()+"' to '"+potentionalSettingsFile.getAbsolutePath()+"'");
			FileOutputStream fos 	= null;
			ObjectOutputStream out 	= null;
			
			/* clear unwanted serialization data */
			instance.beforeSerializing();
			
			try
			{
			  fos = new FileOutputStream(potentionalSettingsFile);
			  out = new ObjectOutputStream(fos);
			  out.writeObject(instance);
			  out.close();
			} catch(IOException ex) {
				log.error ("Failed to write configuration file '" + potentionalSettingsFile.getAbsolutePath()+"'", ex);
			}
		}
		
		
	}
	
	/**
	 * get a SettingsAbstractMaster Tile
	 * @param type
	 * @return a SettingsAbstractMaster Tile
	 */
	public SettingsAbstractMaster get (SettingsConstants.SettingsTypes type) {
		return settings.get(type);
	}
	
	
	
	/**
	 * returns an Iterator for all registered settings types
	 * @return an Iterator <SettingsConstants.SettingsTypes> with all registered types
	 */
	public Iterator <SettingsConstants.SettingsTypes> getRegisteredTypes () {

		ArrayList<SettingsConstants.SettingsTypes> 									re 		
		= new ArrayList<SettingsConstants.SettingsTypes>();
		
		Iterator<Entry<SettingsConstants.SettingsTypes, SettingsAbstractMaster>> 	mapI	
		= settings.entrySet().iterator();

		while (mapI.hasNext()) 
			re.add( mapI.next().getKey() );

		return re.iterator();
	}

	/**
	 * return the registered settings groups
	 * @return
	 */
	public Collection<SettingsAbstractMaster> getRegisteredSettingGroups () {
		return this.settings.values ();
	}

	/**
	 * returns an Iterator for all registered expanders
	 * @return an Iterator<Entry<SettingsConstants.SettingsExpander, ArrayList<SettingsAbstractMaster>>> with every registered expander
	 */
	public Iterator<Entry<SettingsConstants.SettingsExpander, ArrayList<SettingsAbstractMaster>>> getRegisteredExpanders () {
		return settingsToExpander.entrySet().iterator();
	}
	
	/**
	 * get the SettingsElements ArrayList of a specific type
	 * @param type the type of the SettingsElement to be retrieved
	 * @return an Iterator <SettingsElement> with all registered settings elements of a type
	 */
	
	public Iterator <SettingsElement> getSettingsElements (SettingsConstants.SettingsTypes type)
	{
		if (settings.containsKey(type))
			return settings.get(type).getElements().iterator();
		else
			return null;
	}

	/**
	 * save Settings
	 * @return if the serialization has been successful
	 */
	
	public boolean saveSettings () {
		log.debug("Saving Settings ... ");
		Iterator <SettingsConstants.SettingsTypes> i = getRegisteredTypes();
		while (i.hasNext())
			saveTile(i.next());
		
		return true;
	}
	
}

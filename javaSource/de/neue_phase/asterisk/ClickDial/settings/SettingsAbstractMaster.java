package de.neue_phase.asterisk.ClickDial.settings;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;



import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants;

/**
 * SettingsAbstractMaster is the base Class for all Settings
 * it holds a "SettingsElement" for every Setting 
 * 
 * Further more, it will ensure, that NO Control elements
 * will be serialized by calling the "beforeSerializing()" methods
 * of every SettingsElement
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public abstract class SettingsAbstractMaster implements Serializable 
{
	protected SettingsConstants.SettingsTypes    type 		= SettingsConstants.SettingsTypes.none;
	protected SettingsConstants.SettingsExpander expander 	= SettingsConstants.SettingsExpander.Base;

	public 	  final int 	version				 = 8;

	public String						  	name = "Master";

	/* the hash where settings are stored */
	protected final LinkedHashMap<String, SettingsElement> settings = new LinkedHashMap<String, SettingsElement>();

	/* the logging facility
	* - in this case the Logger is static, since it should not be serialized with the settings object. */
	protected static Logger log 				            = Logger.getLogger ("SettingsAbstractMaster");
	
	private static final long serialVersionUID = -2745954613452507927L;

	public SettingsAbstractMaster() {
		initializeSettings();
	}
	
	/* the function meant to be overridden to initialize the "settings" HashMap */
	protected void initializeSettings () {
		
	}
	
	public String 			getValue (String name) 
	{
		if ( ! settings.containsKey(name) || settings.get(name) == null )
		{
			/* element does not exist here */
			return "";
		}
		return settings.get(name).getValue();
	}

	public Integer 			getValueInteger (String name) 
	{
		if ( ! settings.containsKey(name) || settings.get(name) == null )
		{
			/* element does not exist here */
			return 0;
		}
		return new Integer(settings.get(name).getValue());
	}
	
	public SettingsElement 	get	(String name) 
	{
		if ( ! settings.containsKey(name))
			return null;
		
		return settings.get(name);
	}

	/** 
	 * return the defined type
	 * @return the type of this controller
	 */
	public SettingsConstants.SettingsTypes getType() {
		return type;
	}

	/**
	 * return the designated expander
	 * @return the designated expander
	 */
	public SettingsConstants.SettingsExpander getExpander () {
		return expander;
	}
	
	/**
	 * returns a typed ArrayList of all SettingsElement's
	 * @return all SettingsElement's in an ArrayList<SettingsElements>
	 */
	
	public ArrayList<SettingsElement> getElements () {
		
		Iterator<Entry<String,SettingsElement>> i 	= settings.entrySet().iterator();
		ArrayList<SettingsElement> set 				= new ArrayList<SettingsElement>();
		
		while (i.hasNext()) {
			set.add(i.next().getValue());
		}
		
		return set;
	}

	/**
	 * get field names of this SettingsElement
	 * @return field names of this SettingsElement
	 */

	public Set<String> getFieldNames () {
		return this.settings.keySet ();
	}

	/**
	 * function which will be called before the object is put
	 * to disk. This clears unwanted "too-much" serialization
	 */
	
	public void beforeSerializing () {
		Iterator<Entry<String,SettingsElement>> i 	= settings.entrySet().iterator();
		while (i.hasNext()) 
			i.next().getValue().beforeSerializing();
	}
	/**
	 * fill our fields with the ones out of 'from'
	 * @param from the objects which needs migration
	 * @return the new migrated SettingsAbstractMaster
	 */
	public boolean migrateFrom (SettingsAbstractMaster from) {
		Iterator<Entry <String, SettingsElement>> i = settings.entrySet().iterator();
		
		Entry <String, SettingsElement> entry;
		String value;
		while(i.hasNext())
		{
			entry = i.next();
			
			if (from.get(entry.getKey()) != null)
			{
				// -- field exists in old version -> copy (!!) value
				value = from.get(entry.getKey()).getValue();
				log.debug("Migrate field="+entry.getKey()+ " value="+value);
				entry.getValue().setValue( value );
			}
			else
				log.debug ("The field '"+entry.getKey()+"' did not exist in the version "+from.version+" " );
		}
		
		return true;
	}

	/**
	 * set a new value to a field and disable it, since it is controlled by AutoConfig now
	 * @param fieldName
	 * @param newValue
	 * @return true/false for AutoConfig succeeded or not
	 */
	public boolean setAutoConfigField (String fieldName, String newValue) {
		SettingsElement element = this.settings.get (fieldName);
		if (element == null) {
			log.debug ("setAutoConfigField '"+fieldName+"' could not be found?");
			return false;
		}

		if (element.isAutoConfigEnabled ()) {
			element.setValue (newValue);
			element.setDisabled (true);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * return the values fields specified as String[]
	 * @param fields fields which values should be returned
	 * @return values of specified fields
	 */
	public String[] getElementsAsStringArray (String[] fields) {
		String[] returnedFields = new String[fields.length];
		int count = 0;
		for (String field : fields)
			returnedFields[count++] = this.settings.get (field).getValue ();

		return returnedFields;
	}

}

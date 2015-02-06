package de.neue_phase.asterisk.ClickDial.settings;

import java.io.Serializable;
import java.util.HashMap;

import de.neue_phase.asterisk.ClickDial.constants.SettingsConstants.SettingsElementType;

/**
 * a SettingsElement is composed out of the following:
 * 	- name
 *  - value
 *  - corresponding settings field in SettingsWindow widget
 *  
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class SettingsElement  implements Serializable {
	private static final long serialVersionUID = 1561484858796553597L;

	private boolean 			visible = true;
	private boolean 			disabled = false;
	private SettingsElementType type 	= SettingsElementType.text;
	private String  			name	= null;
	private String  			official_name	= null;
	private String				value	= null;
	private HashMap<String, String[]>	specificSettings	= null;
	private boolean				autoConfigField	= false;

	private SettingsViewReferencer				referencingSettingsField = null;

	/**
	 * kill the reference to the SWT.Control View 
	 * there is no need to serialize this thing also :-)
	 */
	public void beforeSerializing () {
		if (referencingSettingsField != null) {
			value = referencingSettingsField.getConfigurationString();
			setReferencingSettingsField(null);
		}
	}
	
	public void setReferencingSettingsField(SettingsViewReferencer referencingSettingsField) {
		this.referencingSettingsField = referencingSettingsField;
	}
	public SettingsViewReferencer getReferencingSettingsField() {
		return referencingSettingsField;
	}
	
	/**
	 * this item is usable, if the referencingSettingsField is NOT
	 * null and if the fields there are NOT disposed!
	 * @return true if the referencing SWT.Control is a usable setting producer
	 */
	public boolean isReferencingFieldUsable() {
		return (referencingSettingsField != null && ! referencingSettingsField.disposed());
	}
	
	public SettingsElement(String name, String official_name, String value) {
		this.name 				= name;
		this.value 				= value;
		this.official_name		= official_name;
		System.out.println("Element: "+ name + " val: '"+ value +"' Hash: "+this.hashCode());
	}
	public SettingsElement(String name, String official_name, String value, SettingsElementType type) {
		this (name, official_name, value); 
		this.type = type;
	}	
	
	/**
	 * set the managed SettingsElementType
	 * @param type
	 */
	
	public void setType(SettingsElementType type) {
		this.type = type;
	}

	public void registerSpecificSetting (String name, String[] value) {
		if (specificSettings == null)
			specificSettings = new HashMap<String, String[]>();
		
		specificSettings.put(name, value);
	}
	
	public String[] getSpecificSetting (String name) {
		return specificSettings.get(name);
	}
	
	public void setValue (String value) {
		this.value = value;
	}

	public String getValue() {
		if (value == null)
			return "";

		return value;
	}
	public String getName() {
		return name;
	}

	public String getOfficialName() {
		return official_name;
	}

	public SettingsElementType getType() {
		return type;
	}
	
	public boolean isVisible() {
		return visible;
	}

	public Boolean isDisabled() {
		return disabled;
	}

	/**
	 * Set the Settings Element disabled
	 * @param newState disabled (TRUE), not disabled (FALSE)
	 */
	public void setDisabled (Boolean newState) {
		this.disabled = newState;
	}

	/**
	 * enable that AutoConfig can set the value of this field.
	 */
	public void enableAutoConfig () {
		this.autoConfigField = true;
	}

	/**
	 * check if this field can be set by AutoConfig.
	 * @return yes/no
	 */
	public boolean isAutoConfigEnabled () {
		return this.autoConfigField;
	}
}

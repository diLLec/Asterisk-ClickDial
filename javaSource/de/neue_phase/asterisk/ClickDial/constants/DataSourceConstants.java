package de.neue_phase.asterisk.ClickDial.constants;

/**
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 * @since 03. Juni 2007
 * constants defined to be used by Datasource Package
 * 
 */

public class DataSourceConstants {
	
	public static enum DataSourceTypes {
		xml,
		outlook,
		mysql,
		ldap
	}

	public final static Integer OutlookDataSource_Cache_Invalidate_Seconds = 600;
	
}

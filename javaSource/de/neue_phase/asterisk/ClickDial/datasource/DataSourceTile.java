package de.neue_phase.asterisk.ClickDial.datasource;

import de.neue_phase.asterisk.ClickDial.controller.listener.DataSourceResultSetListener;
import org.apache.log4j.Logger;

/**
 * a master class for the specific datasource tile implementation
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public abstract class DataSourceTile implements DataSourceTileInterface {
	protected final Logger log 				= Logger.getLogger(this.getClass());
	protected DataSourceResultSetListener resultSetListener = null;
	
	/**
	 * query a generic string to @ the datasource
	 *  - meant to be overridden
	 *  
	 * @param query
	 */
	public void query (String query) {	}
	
	/** 
	 * get the Name of the datasource
	 * @return the name of the datasource
	 */
	public String getName () {
		return "general";
	}


	public void addResultSetListener (DataSourceResultSetListener resultSetListener) {
		this.resultSetListener = resultSetListener;
	}

}

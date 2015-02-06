package de.neue_phase.asterisk.ClickDial.datasource;

import java.util.ArrayList;

import de.neue_phase.asterisk.ClickDial.controller.listener.DataSourceResultSetListener;
import de.neue_phase.asterisk.ClickDial.controller.exception.QueryAlreadyRunningException;
import de.neue_phase.asterisk.ClickDial.util.Dispatcher;
import de.neue_phase.asterisk.ClickDial.util.events.ClickDialEvent;
import de.neue_phase.asterisk.ClickDial.util.events.FindContactEvent;
import de.neue_phase.asterisk.ClickDial.util.events.FoundContactEvent;
import de.neue_phase.asterisk.ClickDial.util.listener.FindContactEventListener;
import org.apache.log4j.Logger;

/**
 * Holder class, to encapsulate and group DataSourceTile's together
 * for consolidated querying
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class DataSourceHolder implements FindContactEventListener, DataSourceResultSetListener {

	/* indicates whether a query is already running here */
	private Boolean queryRunning 							= false;
	private Dispatcher dispatcherRef						= null;
	
	/* holds all registered datasources */
	private final ArrayList<DataSourceTile> dataSources 	= new ArrayList<DataSourceTile>();

	/* sumarizes the current result set */
	private ArrayList<Contact>				resultSet		= null;
	
	/* log */
	protected final Logger log 								= Logger.getLogger(this.getClass());
	
	/**
	 * default constructor
	 *
	 */
	public DataSourceHolder(Dispatcher dispatcherRef) {
		this.dispatcherRef = dispatcherRef;
		dispatcherRef.addEventListener (ClickDialEvent.Type.ClickDial_FindContactEvent, this);
	}
	
	/**
	 * register a new and valid dataSource
	 * @param ds
	 */
	
	public void registerDatasource (DataSourceTile ds)
	{
		log.debug ("Register Datasource: '"+ds.getName ()+"'");
		ds.addResultSetListener (this);
		dataSources.add(ds);
	}
	
	/**
	 * consolidated query function for all registered datasources
	 * @param queryStr
	 * @return the found DataSource results in an ArrayList<String> 
	 */

	public void queryAll (String queryStr) throws QueryAlreadyRunningException
	{

		toggleQueryRunning ( true );
		ArrayList<Thread>		 threadArray = new ArrayList<Thread> ();

		for (DataSourceTile dataSource : dataSources) {
			Thread t = new Thread (() -> dataSource.query (queryStr));
			t.start ();
			threadArray.add (t);
			log.debug("Querying "+ dataSource.getName() +" datasource (Thread "+t.getId ()+")");
		}

		for (Thread thread : threadArray) {
			try {
				log.debug("Waiting on Thread "+thread.getId ()+" to finish)");
				thread.join ();
			}
			catch (InterruptedException e) {}
		}

		toggleQueryRunning (false);
		log.debug("all threads finished");
	}

	@Override
	public synchronized void addResultSet (ArrayList<Contact> rs) {
		if (this.resultSet == null)
			this.resultSet = new ArrayList<Contact>(rs);
		else
			this.resultSet.addAll (rs);
	}

	/**
	 * toggleQueryRunning will switch the queryRunning variable to ensure that there is only one query at a time
	 * @param newState
	 * @throws QueryAlreadyRunningException
	 */
	public void toggleQueryRunning (Boolean newState) throws QueryAlreadyRunningException {
		if (queryRunning == false && newState == true)
			queryRunning = true;

		else if (queryRunning == true && newState == false)
			queryRunning = false;

		else if (queryRunning == true && newState == true)
			throw new QueryAlreadyRunningException ();

		log.debug("Toggle queryRunning to " + newState.toString ());
	}

	/**
	 * handle the FindContactEvent from any consumer and generate a FoundContactEvent after
	 * a search on all registered DataSources
	 * @param event
	 */
	public void handleFindContactEvent (FindContactEvent event) {
		try {
			this.queryAll (event.getSearchString ());
			log.debug ("QueryAll done - result set size:" + this.resultSet.size ());
			dispatcherRef.dispatchEvent (new FoundContactEvent (this.resultSet));
		} catch (QueryAlreadyRunningException e) {
			log.error ("FindContactEvent for " + event.getSearchString () + " can't be handled since a query is already running.");
		}
	}
	/**
	 * close down
	 */
	
	public void closeDown () {

		Boolean closeable = false;
		do {
			try {
				toggleQueryRunning (false);
				closeable = true;
			} catch (QueryAlreadyRunningException e) {
				log.error ("Can't close down DataSourceHolder, since it still waits on a result set.");
				try {
					Thread.sleep (500);
				} catch (InterruptedException ee) {
				}
				closeable = false;
			}
		} while (!closeable);

		this.dataSources.forEach (de.neue_phase.asterisk.ClickDial.datasource.DataSourceTile::close);
	}
}


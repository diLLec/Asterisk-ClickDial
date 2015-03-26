package de.neue_phase.asterisk.ClickDial.datasource;

import java.util.ArrayList;

import com.google.common.eventbus.Subscribe;
import de.neue_phase.asterisk.ClickDial.controller.listener.DataSourceResultSetListener;
import de.neue_phase.asterisk.ClickDial.controller.exception.QueryAlreadyRunningException;
import de.neue_phase.asterisk.ClickDial.eventbus.EventBusFactory;
import de.neue_phase.asterisk.ClickDial.eventbus.events.FindContactEvent;
import de.neue_phase.asterisk.ClickDial.eventbus.events.FoundContactEvent;
import org.apache.log4j.Logger;

/**
 * Holder class, to encapsulate and group DataSourceTile's together
 * for consolidated querying
 * 
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 */

public class DataSourceHolder implements DataSourceResultSetListener {

	/* indicates whether a query is already running here */
	private Boolean queryRunning 							= false;
	
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
	public DataSourceHolder() {
        EventBusFactory.getThradPerTaskEventBus ().register (this);
	}

    /**
     * is the given class already registered in the holder?
     * @param dsClass lookup this class in the registry
     * @return yes/no
     */
    public Boolean isRegistered (Class dsClass) {
        for (DataSourceTile ds : dataSources) {
            if (ds.getClass () == dsClass)
                return true;
        }

        return false;
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
        resultSet                            = new ArrayList<> (); // clear result set

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

    /**
     * callback for all the datasources to add their resultset
     * @param rs
     */
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
	 * @param event an event that indicates what needs to be searched
	 */
	@Subscribe public void handleFindContactEvent (FindContactEvent event) {
		try {
			this.queryAll (event.getSearchString ());
			log.debug ("QueryAll done - result set size:" + this.resultSet.size ());
            log.debug ("posting eventbus: " + EventBusFactory.getDisplayThreadEventBus ().toString ());
            EventBusFactory.getDisplayThreadEventBus ().post (new FoundContactEvent (this.resultSet));
            log.debug ("posted eventbus: " + EventBusFactory.getDisplayThreadEventBus ().toString ());
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


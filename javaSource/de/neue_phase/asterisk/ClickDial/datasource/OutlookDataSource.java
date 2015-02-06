package de.neue_phase.asterisk.ClickDial.datasource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.neue_phase.asterisk.ClickDial.constants.DataSourceConstants;
import org.apache.log4j.Logger;

import com.jacob.activeX.*;
import com.jacob.com.*;

/**
 * An Outlook datasource access class which fetches names and numbers
 * out of the address book
 * 
 * more information, please see:
 * -- http://stackoverflow.com/questions/19939967/java-jacob-outlook-get-all-contacts
 * -- http://www.datalife.com/yitz/automation/ol_constants.html
 * -- http://www.contactgenie.com/outlook_fields.htm
 *
 * @author Michael Konietzny <Michael.Konietzny@neue-phase.de>
 *
 */

public class OutlookDataSource extends DataSourceTile {

	/* register ActiveX Component */
	//private final ActiveXComponent ol = new ActiveXComponent("Outlook.Application");
		
	/* create a dispatch to call the Namespace and an Dispatch Object to save data */
	//private final Dispatch olo 		  				= ol.getObject();
	//private final Dispatch myNamespace 			  	= Dispatch.call(olo, "GetNamespace", "MAPI").toDispatch();
	/* call the folder */
	//final Integer olFolderContacts					= 10;
	//private final Dispatch myFolder 				= Dispatch.call(myNamespace, "GetDefaultFolder", olFolderContacts).toDispatch();
	
	/* the last resultset */
	private final ArrayList<Contact> returnedContacts 	= new ArrayList<Contact>();

	public OutlookDataSource() {}
	
	/* the logging facility */
	private final Logger    log 								= Logger.getLogger(this.getClass());

	/* the contact cache */
	private static ArrayList<Contact>	contactsCache 			= null;
	private static Date cacheInvalidateTime  					= null;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "outlook";
	}

	private synchronized void updateCacheInvalidTime () {
		Calendar cal = Calendar.getInstance ();
		cal.add(Calendar.SECOND, DataSourceConstants.OutlookDataSource_Cache_Invalidate_Seconds);
		cacheInvalidateTime = cal.getTime ();
		log.debug ("Next cache invalidation time: " + cacheInvalidateTime.getTime ());
	}

	private synchronized Boolean isCacheInvalid () {
		if (cacheInvalidateTime == null)
			return true;
		else if (cacheInvalidateTime.before (new Date()))
			return true;
		else
			return false;
	}

	/**
	 * readContactsIntoCache will fill the static contactsCache
	 * from the Global Access List - this function has been abondoned
	 * since there is no way to filter the GAL. In the example @ T-Systems
	 * the GAL ist about 300K entries long and a full cache load would
	 * take >5 minutes.
	 *
	 */
	private synchronized Boolean readContactsIntoCacheFromGAL (String queryString) {

		log.debug ("Outlook Data source is building Cache");

		OutlookDataSource.contactsCache = new ArrayList<Contact> ();

		ActiveXComponent ol;
		Dispatch olo 			= null;
		Dispatch myNamespace 	= null;
		Dispatch myFolder		= null;
		Dispatch items			= null;
		Dispatch filter			= null;

		try {
			ol = new ActiveXComponent ("Outlook.Application");
			olo = ol.getObject ();
		} catch (JacobException ex) {
			log.error ("Failed to get ActiveXComponent.", ex);
			return false;
		}

		try {
			myNamespace = Dispatch.call (olo, "GetNamespace", "MAPI").toDispatch ();
		} catch (JacobException ex) {
			log.error ("Failed to GetNamespace (MAPI)", ex);
			return false;
		}

		int count = 0;
		try {

			//myFolder = Dispatch.call (myNamespace, "GetDefaultFolder", 10).toDispatch ();
			//items = Dispatch.get (myFolder, "Items").toDispatch ();

			// - first try to access the GAL - for now didn't try that one but the performance
			//   must be awkward since there is no direct search function
			// - see https://www.add-in-express.com/forum/read.php?FID=5&TID=11484
			//
			myFolder = Dispatch.call (myNamespace, "GetGlobalAddressList").toDispatch ();
			items = Dispatch.get (myFolder, "AddressEntries").toDispatch ();
		} catch (JacobException ex) {
			log.error ("Failed to change folder to olFolderContacts (10)", ex);
			return false;
		}

		try {
			filter = Dispatch.call (items, "Filter").toDispatch ();
			Dispatch.put (filter, "Name", queryString);
			count = Dispatch.call (items, "Count").getInt ();
			log.debug ("Filtered GSL to " + count + " items.");
		} catch (JacobException ex) {
			log.error ("Was not able to install a filter on AddressEntries", ex);
			return false;
		}

		String displayname, lastname, phone, mobil, company;
		Contact contact;
		Dispatch item = null;

		for (int i = 1; i <= count; i++) {

			try {
				item = Dispatch.call (items, "Item", i).toDispatch ();
			} catch (JacobException ex) {
				log.error ("Failed to get item '"+i+"' out of collection.", ex);
				continue;
			}

			try {
				/*
				firstname = Dispatch.get (item, "FirstName").toString ();
				lastname = Dispatch.get (item, "LastName").toString ();
				company = Dispatch.get (item, "CompanyName").toString ();
				contact = new Contact (firstname, lastname, company);
				phone = Dispatch.get (item, "BusinessTelephoneNumber").toString ();
				privat = Dispatch.get (item, "HomeTelephoneNumber").toString ();
				mobil = Dispatch.get (item, "MobileTelephoneNumber").toString ();
				*/

				Dispatch exchangeUser;
				try {
					exchangeUser = Dispatch.call (item, "GetExchangeUser").toDispatch ();
					displayname = Dispatch.call (item, "Name").toString ();
				} catch (JacobException ex) {
					log.error ("Unable to call GetExchangeUser on item " + i + " - skipped", ex);
					continue;
				}

				company 	= Dispatch.get (exchangeUser, "CompanyName").toString ();
				contact = new Contact (displayname, "", company);

				phone 		= Dispatch.get (exchangeUser, "BusinessTelephoneNumber").toString ();
				mobil 		= Dispatch.get (exchangeUser, "MobileTelephoneNumber").toString ();

				if (phone != null && phone.length () > 0)
					contact.addPhoneNumber (phone, Contact.PhoneType.businessPhone);

				if (mobil != null && mobil.length () > 0)
					contact.addPhoneNumber (mobil, Contact.PhoneType.mobile);

				if (contact.hasPhoneNumbers ()) {
					log.debug (contact);
					OutlookDataSource.contactsCache.add (contact);
				}
			} catch (JacobException ex) {
				log.error ("Failed to query information details of item '"+i+"'.", ex);
			} catch (Exception ex) {
				log.error ("Failed to create Contact object from outlook contact", ex);
			}
		}

		log.debug ("Cache building done. '"+OutlookDataSource.contactsCache.size ()+"' Contacts in cache");

		this.updateCacheInvalidTime ();
		return true;
	}

	/**
	 * readContactsIntoCache will fill the static contactsCache
	 * from the Global Access List - this function has been abondoned
	 * since there is no way to filter the GAL. In the example @ T-Systems
	 * the GAL ist about 300K entries long and a full cache load would
	 * take >5 minutes.
	 * - see: https://social.msdn.microsoft.com/Forums/de-DE/1d439e92-ac63-4c82-bae6-19bd9858404c/how-to-filter-in-global-address-list-using-outlook-object-model
	 *   ... and no - we don't bundle Redemption!
	 *
	 * - maybe WEBDAV is a way?
	 *   see: http://weblogs.asp.net/whaggard/how-do-i-access-my-outlook-contacts-from-my-web-application
	 */
	private synchronized Boolean readContactsIntoCache () {

		log.debug ("Outlook Data source is building Cache");

		OutlookDataSource.contactsCache = new ArrayList<Contact> ();

		ActiveXComponent ol;
		Dispatch olo 			= null;
		Dispatch myNamespace 	= null;
		Dispatch myFolder		= null;
		Dispatch items			= null;

		try {
			ol = new ActiveXComponent ("Outlook.Application");
			olo = ol.getObject ();
		} catch (JacobException ex) {
			log.error ("Failed to get ActiveXComponent.", ex);
			return false;
		}

		try {
			myNamespace = Dispatch.call (olo, "GetNamespace", "MAPI").toDispatch ();
		} catch (JacobException ex) {
			log.error ("Failed to GetNamespace (MAPI)", ex);
			return false;
		}

		int count = 0;
		try {

			myFolder = Dispatch.call (myNamespace, "GetDefaultFolder", 10).toDispatch ();
			items = Dispatch.get (myFolder, "Items").toDispatch ();
			count = Dispatch.call (items, "Count").getInt ();
		} catch (JacobException ex) {
			log.error ("Failed to change folder to olFolderContacts (10)", ex);
			return false;
		}

		String firstname, lastname, phone, mobil, company;
		Contact contact;
		Dispatch item = null;

		for (int i = 1; i <= count; i++) {

			try {
				item = Dispatch.call (items, "Item", i).toDispatch ();
			} catch (JacobException ex) {
				log.error ("Failed to get item '"+i+"' out of collection.", ex);
				continue;
			}

			try {

				firstname = Dispatch.get (item, "FirstName").toString ();
				lastname = Dispatch.get (item, "LastName").toString ();
				company = Dispatch.get (item, "CompanyName").toString ();

				contact = new Contact (firstname, lastname, company);
				phone = Dispatch.get (item, "BusinessTelephoneNumber").toString ();
				mobil = Dispatch.get (item, "MobileTelephoneNumber").toString ();

				if (phone != null && phone.length () > 0)
					contact.addPhoneNumber (phone, Contact.PhoneType.businessPhone);

				if (mobil != null && mobil.length () > 0)
					contact.addPhoneNumber (mobil, Contact.PhoneType.mobile);

				if (contact.hasPhoneNumbers ()) {
					log.debug (contact);
					OutlookDataSource.contactsCache.add (contact);
				}
			} catch (JacobException ex) {
				log.error ("Failed to query information details of item '"+i+"'.", ex);
			} catch (Exception ex) {
				log.error ("Failed to create Contact object from outlook contact", ex);
			}
		}

		log.debug ("Cache building done. '"+OutlookDataSource.contactsCache.size ()+"' Contacts in cache");

		this.updateCacheInvalidTime ();
		return true;
	}

	@Override
	public void query (String queryStr) {

		if (this.isCacheInvalid ())
			readContactsIntoCache ();

		/* clean the array before we start */
		log.debug ("This is the outlook ds. We search for '" + queryStr + "'");
		returnedContacts.clear ();

		for (Contact c : OutlookDataSource.contactsCache) {
			if (c.firstname.contains (queryStr) || c.lastname.contains (queryStr) || c.company.contains (queryStr))
				returnedContacts.add (c);
		}

		log.debug ("found '"+returnedContacts.size ()+"' contacts that matches '"+queryStr+"'");
		resultSetListener.addResultSet (returnedContacts);
	}



	public void close() {
		log.debug(getName() + " Datasource closing down resources");
	}

}

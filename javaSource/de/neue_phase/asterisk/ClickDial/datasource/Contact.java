package de.neue_phase.asterisk.ClickDial.datasource;

import org.asteriskjava.live.CallerId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by mky on 21.01.2015.
 */
public class Contact {
    public String firstname, lastname, company;
    public static enum PhoneType {
        mobile,
        businessPhone
    }
    public HashMap<PhoneType, ArrayList<String>> phoneNumbers = new HashMap<PhoneType, ArrayList<String>> ();

    public Contact (String firstname, String lastname, String company) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.company = company;
    }

    public void addPhoneNumber (String phoneNumber, PhoneType phoneNumberType) {
        if (phoneNumbers.containsKey (phoneNumberType))
            phoneNumbers.get (phoneNumberType).add (phoneNumber);
        else {
            ArrayList<String> newNumbersList = new ArrayList<String> ();
            newNumbersList.add (phoneNumber);
            phoneNumbers.put (phoneNumberType, newNumbersList);
        }
    }

    /**
     * get the contact as list of strings for all matching numbers
     * @return
     */
    public ArrayList<String> getStringRepresentation () {
        ArrayList<String> returnString = new ArrayList<String>();

        for (Map.Entry<PhoneType, ArrayList<String>> entry : phoneNumbers.entrySet()) {

            for (String phoneNumber : entry.getValue()) {
                CallerId c = new CallerId (this.firstname + " " + this.lastname + "("+entry.getKey()+")", phoneNumber);
                returnString.add (c.toString ());
            }
        }
        return returnString;
    }

    public String toString () {
        return this.firstname + " " + this.lastname + " (" + phoneNumbers.size () + " phone numbers)";
    }

    public Boolean hasPhoneNumbers () {
        return (this.phoneNumbers.size () > 0);
    }

}

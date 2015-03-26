package de.neue_phase.asterisk.ClickDial.controller.util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AsteriskCallerid
 * This class is a model for the callerid used in every context
 * @author Michael Konietzny <michael.konietzny@t-systems.com>
 */
public class AsteriskCallerId {
    private String number;
    private String name;
    private final static String DEFAULT_NAME = "-";
    private final static String DEFAULT_NUM  = "0";

    public AsteriskCallerId (String name, String number) {
        this.number = number;
        this.name  = name;
    }


    /**
     * toString
     * The magic function which is called every time an object
     * of this class gets used as a string. We return the full
     * formatted callerid string if that happens.
     * @return The formatted callerid or either of one field.
     */
    public String toString () {
        return formatCallerid(name, number);
    }

    /**
     * formatCallerid
     * Returns the callerid (number and name) in the string format ("name" <number>)
     * @param name The name part
     * @param number The number part
     * @return The formatted string ("name" <number>)
     */
    private String formatCallerid ( String name, String number ) {

        if (number.equals ("<>"))
            number = ""; // empty it

        if(name.length () > 0 && number.length () > 0) {
            // the default
            return "\"" + name + "\" <"+ number +">";
        }
        else if(name.length () > 0) {
            // should not happen
            return "\""+ name +"\" <>";
        }
        else if(number.length () > 0 ) {
            // return the number as name when no name is
            // defined - some phones only display the
            // name
            return "\""+ number +"\" <"+ number +">";
        }
        else {
            return "";
        }

    }

    /**
     * isFull
     * Signals that we have data for name as well as for number
     * @return boolean We have both number and name (TRUE) or not (FALSE)
     */
    public Boolean isFull () {
        return (this.hasName () && hasNumber ());
    }

    /**
     * hasName
     * Signals that we at least have the name
     * @return boolean We have a name (TRUE) or not (FALSE)
     */
    public Boolean hasName () {
        return (this.name.length () > 0 && this.name.equals (AsteriskCallerId.DEFAULT_NAME));
    }

    /**
     * hasNumber
     * Signals that we at least have the number
     * @return boolean We have a number (TRUE) or not (FALSE)
     */
    public Boolean hasNumber () {
        return (this.number.length () > 0);
    }

    /**
     * calleridFromString
     * Check/Parse the callerid string and return
     * the two facts name and number in an array
     * @param input The callerid in the format "name" <number>
     * @return either a constructed AsteriskCallerId object or null
     */
    public static AsteriskCallerId calleridFromString (String input) {

        Pattern p;
        Matcher m;

        p = Pattern.compile ("\"?([^\"']+)\"? <([0-9A-Za-z]+)>");
        m = p.matcher (input);
        if (m.matches ())
            return new AsteriskCallerId(m.group (1), m.group (2));

        p = Pattern.compile ("'?([^']+)'? <([0-9A-Za-z]+)>");
        m = p.matcher (input);
        if (m.matches ())
            return new AsteriskCallerId(m.group (1), m.group (2));

        p = Pattern.compile ("<([0-9A-Za-z]+)>");
        m = p.matcher (input);
        if (m.matches ())
            return new AsteriskCallerId(m.group (1), m.group (1));

        p = Pattern.compile ("^([0-9]+)$");
        m = p.matcher (input);
        if (m.matches ())
            return new AsteriskCallerId(m.group (1), m.group (1));

        return null;
    }

    /**
     * defaultCallerid
     * Return the default empty callerid
     */
    public static AsteriskCallerId defaultCallerid () {
        return new AsteriskCallerId (AsteriskCallerId.DEFAULT_NAME, AsteriskCallerId.DEFAULT_NUM);
    }

    /**
     * get the number tile of the CallerId
     * @return the number
     */
    public String getNumber () {
        return number;
    }

    /**
     * get the name tile of the CallerId
     * @return the name
     */
    public String getName () {
        return name;
    }
}

package org.apache.tomcat.task;

import java.util.Date;
import java.util.Locale;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * A parser for date strings commonly found in http and email headers that
 * follow various RFC conventions.  Given a date-string, the parser will
 * attempt to parse it by trying matches with a set of patterns, returning
 * null on failure, a Date object on success.
 *
 * @author Ramesh.Mandava 
 */
public class RfcDateParser {
    private static final String debugProp = "hotjava.debug.RfcDateParser";

    private boolean isGMT = false;

    private static boolean usingJDK = false;

    static final String[] standardFormats = {
	"EEEE', 'dd-MMM-yy HH:mm:ss z",   // RFC 850 (obsoleted by 1036)
	"EEEE', 'dd-MMM-yy HH:mm:ss",     // ditto but no tz. Happens too often
	"EEE', 'dd-MMM-yyyy HH:mm:ss z",  // RFC 822/1123
	"EEE', 'dd MMM yyyy HH:mm:ss z",  // REMIND what rfc? Apache/1.1
	"EEEE', 'dd MMM yyyy HH:mm:ss z", // REMIND what rfc? Apache/1.1
	"EEE', 'dd MMM yyyy hh:mm:ss z",  // REMIND what rfc? Apache/1.1
	"EEEE', 'dd MMM yyyy hh:mm:ss z", // REMIND what rfc? Apache/1.1
	"EEE MMM dd HH:mm:ss z yyyy",      // Date's string output format
	"EEE MMM dd HH:mm:ss yyyy",	  // ANSI C asctime format()
	"EEE', 'dd-MMM-yy HH:mm:ss",      // No time zone 2 digit year RFC 1123
 	"EEE', 'dd-MMM-yyyy HH:mm:ss"     // No time zone RFC 822/1123
    };

    /* because there are problems with JDK1.1.6/SimpleDateFormat with
     * recognizing GMT, we have to create this workaround with the following
     * hardcoded strings */
    static final String[] gmtStandardFormats = {
	"EEEE',' dd-MMM-yy HH:mm:ss 'GMT'",   // RFC 850 (obsoleted by 1036)
	"EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'",  // RFC 822/1123
	"EEE',' dd MMM yyyy HH:mm:ss 'GMT'",  // REMIND what rfc? Apache/1.1
	"EEEE',' dd MMM yyyy HH:mm:ss 'GMT'", // REMIND what rfc? Apache/1.1
	"EEE',' dd MMM yyyy hh:mm:ss 'GMT'",  // REMIND what rfc? Apache/1.1
	"EEEE',' dd MMM yyyy hh:mm:ss 'GMT'", // REMIND what rfc? Apache/1.1
	"EEE MMM dd HH:mm:ss 'GMT' yyyy"      // Date's string output format
    };

    String dateString;

    public RfcDateParser(String dateString) {
	this.dateString = dateString.trim();
	if (this.dateString.indexOf("GMT") != -1) {
	    isGMT = true;
	}

	// use java.text.SimpleDateFormat if present
	try {
	    Class c = Class.forName("java.text.SimpleDateFormat");
	    usingJDK = true;
	} catch (ClassNotFoundException e) {
	}
	
    }

    public Date getDate() {

        if (usingJDK == true) {
	    int arrayLen = isGMT ? gmtStandardFormats.length : standardFormats.length;
	    for (int i = 0; i < arrayLen; i++) {
		Date d = null;

		if (isGMT) {
		    d = tryParsing(gmtStandardFormats[i]);
		} else {
		    d = tryParsing(standardFormats[i]);
		}
		if (d != null) {
		    return d;
		}

	    }

	    return null;

	}  // end clause "if (usingJDK)"

	return parseNoJDKDate();

    }    

    private Date tryParsing(String format) {

	java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(format, Locale.US);
	if (isGMT) {
	    df.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	try {
		return df.parse(dateString);
	} catch (Exception e) {
	    return null;
	}
    }

    private Date parseNoJDKDate() {
	// format is wdy, DD-Mon-yyyy HH:mm:ss GMT
	// or
	// format is wdy, DD-Mon-yy HH:mm:ss GMT
	Date fInternalDate = null;
	
	try {
	    fInternalDate  = new Date(dateString);
	}
	catch (Exception ex) {
	}
	
	// Apply emergency parsing measures to work around
	// Y2K 2 digit year date parse bug in java.util.Date
	// format is wdy, DD-Mon-yy HH:mm:ss GMT
	
	if ( fInternalDate == null ) {
	    String newString = new String();
	    
	    StringTokenizer spaces = new StringTokenizer(dateString, " ");
	    
	    if ( spaces.countTokens() >= 3 ) {
		newString = newString.concat(spaces.nextToken());
		
		String DDMonyy = spaces.nextToken();
		int idx = DDMonyy.lastIndexOf('-');
		String DDMon = DDMonyy.substring(0, idx);
		
		if ( idx >= 0 ) {
		    String year = DDMonyy.substring(idx+1);
		    
		    if ( year.length() == 2 ) {
			
			try { 
			    int yearInt = Integer.parseInt(year);
			    
			    if ( yearInt < 70 ) {
				yearInt = yearInt+2000;
				String newYY = Integer.toString(yearInt);
				
				newString = newString.concat(" " + DDMon + "-" + newYY);
			    }
			    else {
				return(null);
			    }
			}
			catch ( Exception ex ) {
			    return(null);
			}
		    }
		    else {
			return(null);
		    }
		}
		else {
		    return(null);
		}

		while ( spaces.hasMoreTokens() ) {
		    newString = newString.concat( " " + spaces.nextToken() );
		}

		try {
		    fInternalDate  = new Date(newString);
		}
		catch (Exception ex) {
		}
	    }  // end of if (spaces.countTokens() >= 3)
	    else {
		return(null);
	    }
	}  // end of if (fInternalDate == null)

	return fInternalDate;
    }

} /* class RfcDateParser */

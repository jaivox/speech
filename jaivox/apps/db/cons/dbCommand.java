/*
   Jaivox version 0.4 April 2013
   Copyright 2010-2013 by Bits and Pixels, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

import java.util.Vector;
import java.util.StringTokenizer;
import com.jaivox.interpreter.Command;
import com.jaivox.interpreter.HistNode;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class dbCommand extends Command {

	String category;
	String function;

	// replace with your location, remove the following line which
	// causes a compilation error
	PleaseChangeDbfileLocation
	static String dbfile = "/home/you/.mozilla/firefox/t8a8aqor.default/cookies.sqlite";
	static String dbspec = "jdbc:sqlite:" + dbfile;
	Connection connection = null;
	Statement statement = null;

	boolean initialized = false;

	public dbCommand () {
		super ();
		boolean ok = setDriverParams ();
		if (!ok) {
			System.out.println ("Sqlite is not set up properly for this example.");
			return;
		}
		else initialized = true;
	}

	boolean setDriverParams () {
		try {
			// adapted from Xerial example
			// load the sqlite-JDBC driver using the current class loader
			Class.forName("org.sqlite.JDBC");
			// is this the first time? see if the database file exists
			File test = new File (dbfile);
			boolean docreate = true;
			if (test.exists ()) docreate = false;
			// the following will create the file anyway
			connection = DriverManager.getConnection (dbspec);
			statement = connection.createStatement ();
			statement.setQueryTimeout (30);  // set timeout to 30 sec.
			if (docreate) {
				String c1 = "create table action (procid string, reqid string, refid string, ";
				String c2 = "time string, type string, status string, detail string)";
				statement.executeUpdate (c1+c2);
			}
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace ();
			return false;
		}
	}

	String executeQuery (String query) { // throws
		// QueryFailedException{
		try {
			ResultSet rs = statement.executeQuery (query);
			ResultSetMetaData rsMetaData = rs.getMetaData ();
			int ncols = rsMetaData.getColumnCount ();
			StringBuffer sb = new StringBuffer ();
			while (rs.next()) {
				for (int i=1; i<=ncols; i++) {
					String s = rs.getString (i);
					if (i>1) sb.append (",");
					sb.append (s);
				}
				sb.append ("\n");
			}
			String result = new String (sb);
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "Error";
		}
	}

	public String [] handle (String f,
		String question, String spec, String instate,
		Vector <HistNode> history) {
		if (f.equals ("sqlite"))
			return sqlite (question, spec, instate, history);
		else return null;
	}

	String [] sqlite (String question, String spec, String instate, Vector <HistNode> history) {
		String result [] = new String [1];
		result [0] = "";
		if (spec.equals ("(sqlite mostCookies)")) {
			result [0] = mostCookies ();
		}
		if (spec.equals ("(sqlite analyticRatio)")) {
			result [0] = analyticRatio ();
		}
		if (spec.equals ("(sqlite recentRatio)")) {
			result [0] = recentRatio ();
		}
		return result;
	}

	String mostCookies () {
		try {
			String query = "select baseDomain, count(*) as frequency "+
					"from moz_cookies group by " +
					"baseDomain order by count(*) desc";
			String data = executeQuery (query);
			StringTokenizer st = new StringTokenizer (data, ",\r\n");
			if (!st.hasMoreTokens ()) {
				return "Seems like no site has placed cookies";
			}
			String site = st.nextToken ();
			return site + " seems to have the most cookies.";
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "There was an error in finding the site with the most cookies";
		}
	}

	// gets the Google analytics cookies that start with __utm

	String analyticRatio () {
		try {
			String query = "select name from moz_cookies";
			String data = executeQuery (query);
			StringTokenizer st = new StringTokenizer (data, "\n");
			int n = st.countTokens ();
			if (n == 0) {
				return "Seems like nobody has any cookies";
			}
			int count = 0;
			for (int i=0; i<n; i++) {
				String s = st.nextToken ();
				if (s.startsWith ("__utm")) count++;
			}
			String result = getRatioWords (count, n);
			return result +" of the cookies are for anlytics";
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "There was an error finding the proportion of analytic cookies";
		}
	}

	String getRatioWords (int num, int denom) {
		double x = (double)num/(double)denom;
		if (x > 3.0/4.0) {
			return "more than three fourths";
		}
		else if (x > 1.0/2.0) {
			return "more than half";
		}
		else if (x > 1.0/3.0) {
			return "more than a third";
		}
		else if (x > 1.0/4.0) {
			return "more than a fourth";
		}
		else if (x > 1.0/5.0) {
			return "more than a fifth";
		}
		else return "less than a fifth";
	}

	// we will interpret recent to mean within a week of the
	// current day

	String recentRatio () {
		try {
			java.util.Date today = new java.util.Date ();
			long time = today.getTime ();
			long week = 7L * 24L * 60L * 60L * 1000L;
			long start = time - week;
			long check = start * 1000L; // microseconds
			String query = "select lastAccessed from moz_cookies";
			String data = executeQuery (query);
			StringTokenizer st = new StringTokenizer (data, "\n");
			int n = st.countTokens ();
			if (n == 0) {
				return "Seems like nobody has any cookies";
			}
			int count = 0;
			for (int i=0; i<n; i++) {
				String s = st.nextToken ();
				Long when = Long.parseLong (s);
				if (when > check) count++;
			}
			String result = getRatioWords (count, n);
			return result +" of the cookies were modified within the last week";
		}
		catch (Exception e) {
			e.printStackTrace ();
			return "There was some error determining proportion of recent cookies";
		}
	}

}


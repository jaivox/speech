
package com.jaivox.tools;
import com.jaivox.util.Log;

/**
 * Main program for generating an application.
 * If generated questions have been modified by hand, then call
 * "com.jaivox.tools.Jvgen whatever.conf -update" 
 * instead of "com.jaivox.tools.Jvgen whatever.conf"
 */

public class Jvgen {

	public static void main (String args []) {
		new Log ();
		Log.setLevelByName ("INFO");
		if (args.length == 0) {
			System.out.println ("syntax \"java com.jaivox.tools.Jvgen conf_file [-update]\"");
			return;
		}
		Generator gen = new Generator (args [0]);
		if (args.length > 1) {
			if (args[1].toLowerCase ().equals ("-update")) {
				gen.updateLmQuestions ();
			}
			else {
				System.out.println ("\"Jvgen conf_file -update\" is the only option.");
			}
		}
		else {
			gen.generateAll ();
			gen.createQuestions ();
		}
	}
};

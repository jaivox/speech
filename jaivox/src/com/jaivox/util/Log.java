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
package com.jaivox.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Creates a logger that can then be used to log messages at various
 * levels.
 */

public class Log {

	public static Logger logger;
	
	static Handler console;
	static Handler file;
	
	public static boolean initialized = false;
	static boolean reported = false;
	
/**
 * It is not necessary to create a logger. If there is no logger present,
 * you get one message asking to create a logger. If a logger is created,
 * you will get the messages at the selected level of fineness on the
 * console.
 * We create the logger with the FINEST level at first, thus after a
 * logger is created, the creating program should set the logging level
 * to the desired level of fineness.
 */
	public Log () {
		initialize ();
	}
	
	void initialize () {
		// set this as a top level logger, to control console level
		logger = Logger.getLogger ("com.jaivox.util.Log");
		logger.setUseParentHandlers (false);
		Handler hConsole = new ConsoleHandler ();
		hConsole.setFormatter (new Logformat ());
		hConsole.setLevel (Level.FINEST);
		logger.addHandler (hConsole);
		logger.setLevel (Level.FINEST);
	    // what is the level of this logger?
	    String s = logger.getLevel ().toString ();
		System.out.println ("Logger log level after creating "+s);
		initialized = true;
	}
	
	static boolean check () {
		if (!initialized) {
			if (!reported) {
				System.out.println ("Please create a com.jaivox.util.Log object");
				reported = true;
			}
			return false;
		}
		else return true;
	}
	
/**
 * Log the given message at the given level
@param level
@param s
 */
	public static void log (Level level, String s) {
		if (check ()) logger.log (level, s);
	}
	
/**
 * Set the logging level using a Level value.
@param level
 */
	public static void setLevel (Level level) {
		if (check ()) logger.setLevel (level);
	}
	
/**
 * Set the logging level using one of the words "severe", "warning" etc
 * corresponding to logging levels. This function is case insensitive.
@param word
 */
	
	public static void setLevelByName (String word) {
		String s = word.toUpperCase ();
		Level level = Level.SEVERE;
		if (s.equals ("SEVERE")) level = Level.SEVERE;
		if (s.equals ("WARNING")) level = Level.WARNING;
		if (s.equals ("INFO")) level = Level.INFO;
		if (s.equals ("CONFIG")) level = Level.CONFIG;
		if (s.equals ("FINE")) level = Level.FINE;
		if (s.equals ("FINER")) level = Level.FINER;
		if (s.equals ("FINEST")) level = Level.FINEST;
		logger.setLevel (level);
		Level ll = logger.getLevel ();
		System.out.println ("Logging level now "+ll.toString ());
	}
	
/**
 * The default logger sends logging messages to the screen. This function
 * can be used to designate a file also as a log output.
@param filename
 */
	public static void addFileLog (String filename) {
		if (!check ()) return;
		try {
			file = new FileHandler (filename);
			logger.addHandler (file);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

/**
 * Open an existing file and log to that.
@param filename
 */
	public static void openFile (String filename) {
		if (!check ()) return;
		try {
			file = new FileHandler (filename);
			logger.addHandler (file);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

/**
 * Stop logging to the console, i.e. screen
 */
	public static void clearConsole () {
		if (check ()) logger.removeHandler (console);
	}
	
/**
 * Stop logging to a file
 */
	public static void clearFile () {
		if (check ()) {
			file.close ();
			logger.removeHandler (file);
		}
	}

/**
 * Close the file log if any
 */
	public static void closeFile () {
		if (check ()) {
			file.close ();
			logger.removeHandler (file);
		}
	}

/**
 * Log Level.FINE messages
@param s
 */
	
	public static void fine (String s) {
		if (check ()) logger.fine (s);
	}
	
	/**
	 * Log Level.FINER messages
	@param s
	 */
		
	public static void finer (String s) {
		if (check ()) logger.finer (s);
	}
	
	/**
	 * Log Level.FINEST messages
	@param s
	 */
		
	public static void finest (String s) {
		if (check ()) logger.finest (s);
	}
	
	/**
	 * Log Level.INFO messages
	@param s
	 */
		
	public static void info (String s) {
		if (check ()) logger.info (s);
	}
	
	/**
	 * Log Level.SEVERE messages
	@param s
	 */
		
	public static void severe (String s) {
		if (check ()) logger.severe (s);
	}
	
	/**
	 * Log Level.WARNING messages
	@param s
	 */
		
	public static void warning (String s) {
		if (check ()) logger.warning (s);
	}
	
}

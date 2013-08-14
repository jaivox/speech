/*
   Jaivox version 0.5 August 2013
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

import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Records specific statements along with a simple time format.
 * This is primarily used to log questions and answers from the
 * interpreter. This uses a RandomAccessFile so that we know what was
 * done even if the system crashes.
 */

public class Recorder {

	static String logdir = "./";
	static String terms = " \t\r\n!@$#$%^&*()_-~`+={}[]|\\:;\"\'<>,.?/";

	static RandomAccessFile R;
	
	public static boolean recording = false;

	static SimpleDateFormat stamp;
	
/**
 * Create a recorder with a specific name stub. The full name will be
 * the logdirectory + stub + date + ".txt" 
@param stub
 */
	public Recorder (String stub) {
		if (stub == null) {
			recording = false;
			return;
		}
		startRecorder (stub);
	}
	
/**
 * Start recording
@param stub
 */
	public static void startRecorder (String stub) {
		try {
			stamp = new SimpleDateFormat ("HH:mm:ss");
			SimpleDateFormat dateformat = new SimpleDateFormat ("yyMMdd");
			Date d = new Date ();
			String s = dateformat.format (d);
			String name = logdir + stub + s +".txt";
 			R = new RandomAccessFile (name, "rw");
			System.out.println ("Log file: "+name);
			long N = R.length ();
			R.seek (N);
			recording = true;
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

/**
 * Record a specific string
@param s
 */
	public static void record (String s) {
		try {
			if (!recording) return;
            Date now = new Date ();
            String ts = stamp.format (now);
			R.writeBytes (ts);
			R.writeBytes (" ");
			R.writeBytes (s);
			R.writeBytes ("\n");
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

/**
 * Stop recording. This is not strictly necessary since the RandomAccessFile
 * will keep most of the information that has been logged.
 */
	public static void endRecord () {
		try {
			if (!recording) return;
			if (R != null) R.close ();
			if (recording) recording = false;
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}
}

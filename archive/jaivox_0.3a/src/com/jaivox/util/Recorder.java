package com.jaivox.util;

import java.io.*;
import java.util.*;
import java.text.*;

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

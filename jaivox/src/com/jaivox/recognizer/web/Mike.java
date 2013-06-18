
package com.jaivox.recognizer.web;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Timer;
import com.jaivox.util.Log;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;


public class Mike extends Thread {

	String stub;
	TargetDataLine channel;
	AudioFormat audioformat;
	DataLine.Info info;
	MikeCapture capture;
	java.util.Timer timer;
	String extension;
	int lcount;
	static int period = 5000;


	public Mike (String filestub, String ext) {
		try {
			stub = filestub;
			extension = ext;
			lcount = 1;
			audioformat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				44100.0F, 16, 1, 2, 44100.0F, false);
			info = new DataLine.Info (TargetDataLine.class, audioformat);
			channel = (TargetDataLine) AudioSystem.getLine (info);
		}
		catch (Exception e) {
			e.printStackTrace ();
			Log.severe ("Sorry: could not create recorder");
			return;
		}
	}

	public String nextsample (String format, int seconds) {
		try {
			timer = new Timer ();
			long wait = (long)seconds * 1000L;
			String soundfile = stub + "_" + lcount + format;
			capture = new MikeCapture (this, soundfile);
			// showtime ("Before scheduling");
			timer.schedule (capture, wait, period);
			showtime ("Before recording");
			capture.startrecording ();
			while (channel.isRunning ()) {
				sleep (100);
			}
			lcount++;
			String result = convert (soundfile);
			return result;
		}
		catch (Exception e) {
			e.printStackTrace ();
			Log.severe ("Error trying to record");
			return "error";
		}
	}

	String convert (String wavfile) {
		int n = wavfile.length ();
		String flac = wavfile.substring (0, n-4) + ".flac";
		String cmd = "sox "+wavfile+" "+flac+" rate 16k";
		String result = runcommand (cmd);
		if (result != null)
			return flac;
		else return "error";
	}

	String runcommand (String input) {
		try {
			Process p = Runtime.getRuntime ().exec (input);
			StringBuffer buffer = new StringBuffer ();
			InputStream in = p.getInputStream ();
			BufferedInputStream d = new BufferedInputStream (in);
			do {
				int ch = d.read ();
				if (ch == -1)
					break;
				buffer.append ((char) ch);
			} while (true);
			in.close ();
			String temp = new String (buffer);
			return temp;
		} catch (Exception e) {
			e.printStackTrace ();
			return null;
		}
	}

	public void closechannel () {
		try {
			channel.close ();
			timer.cancel ();
	   		// showtime ("Stopped the timer");
		}
		catch (Exception e) {
			e.printStackTrace ();
			System.exit (0);
		}
	}

	public void showtime (String msg) {
		Date time = new Date ();
		System.out.println (msg + " "+time.toString ());
	}

}



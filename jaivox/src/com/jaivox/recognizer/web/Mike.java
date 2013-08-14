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

/**
 * The Mike class implements a microphone. It captures audio spoken though
 * the microphone and converts it into a file that can be used either for
 * recognition over the web.
 * 
 * Google's web recognizer expects .flac or .speex files. We have found that
 * .flac works better. the Mike class uses MikeCapture to get the audio, then
 * uses a command calling sox to convert the captured (wav file) into the
 * right format.
 */

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

	
/**
 * Create files from the mike. The filestub will be used to name the files
 * in order. The ext is desired extension of the file. Each sample is
 * captured using nextsample.
 * @param filestub
 * @param ext
 */

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
	
/**
 * Get the next sample. It will use the filestub specified
 * when Mike was created. The seconds argument is the number of
 * seconds of silence that will terminate this sample.
 * @param format
 * @param seconds
 * @return
 */

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

	
/**
 * Close this capturing channel
 */
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
	
/** A simple function to display the time along with a message
 * @param msg
 */

	public void showtime (String msg) {
		Date time = new Date ();
		System.out.println (msg + " "+time.toString ());
	}

}



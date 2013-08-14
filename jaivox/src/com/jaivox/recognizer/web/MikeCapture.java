
package com.jaivox.recognizer.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.TimerTask;
import com.jaivox.util.Log;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;


/**
 * This class is used by Mike to capture audio. The recorded audio is
 * converted into the flac format in the Mike class.
 * 
 */
public class MikeCapture extends TimerTask {

	Mike parent;
	String filename;
	AudioFormat audioformat;
	AudioFileFormat.Type type;
	TargetDataLine channel;
	byte buffer [];
	int bufferSize;
	ByteArrayOutputStream byteOut;
	static int silences = 2;
	double last [];
	static double huge = Double.MAX_VALUE;
	public boolean validType;
	boolean stopped = false;

/**
 * Capture audio into the named file.
 * @param p
 * @param name
 */
	public MikeCapture (Mike p, String name) {
		super ();
		parent = p;
		channel = parent.channel;
		filename = name;
		int pos = filename.lastIndexOf (".");
		validType = true;
		if (pos != -1) {
			String ext = filename.substring (pos+1).toLowerCase ().trim ();
			if (ext.equals ("aifc")) {
				type = AudioFileFormat.Type.AIFC;
			}
			else if (ext.equals ("aiff")) {
				type = AudioFileFormat.Type.AIFF;
			}
			else if (ext.equals ("au")) {
				type = AudioFileFormat.Type.AU;
			}
			else if (ext.equals ("snd")) {
				type = AudioFileFormat.Type.SND;
			}
			else if (ext.equals ("wav")) {
				type = AudioFileFormat.Type.WAVE;
			}
			else {
				validType = false;
			}
		}
		else {
			validType = false;
		}

	}

/**
 * startrecording must be called to start the recording. This is called
 * from Mike.nextsample
 */
	public void startrecording () {
		try {
			audioformat = parent.audioformat;
			channel.open (audioformat);
			channel.start ();
			bufferSize = (int)(audioformat.getSampleRate () * audioformat.getFrameSize ());
			buffer = new byte [bufferSize];
			// parent.showtime ("Before asking to record");
			System.out.println ("Speak or hit control-C");
			last = new double [silences];
			for (int i=0; i<silences; i++) last [i] = huge;
			listenToWrite ();
			// AudioInputStream incoming = new AudioInputStream (channel);
			// AudioSystem.write (incoming, AudioFileFormat.Type.WAVE, stream);
		}
		catch (Exception e) {
			e.printStackTrace ();
			Log.severe ("Error recording: "+e.toString ());
		}
	}

	public void run () {
		try {
			parent.showtime ("Time is up");
			stopped = true;
			channel.flush ();
			channel.stop ();
			int chop = chopSeconds (last);
			writeBytes (chop);
			parent.closechannel ();
		}
		catch (Exception e) {
			e.printStackTrace ();
			Log.severe ("Error recording: "+e.toString ());
		}
	}

	void listenToWrite () {
		try {
			byteOut = new ByteArrayOutputStream ();

			while (true) {
				int read = 0;
				if (!stopped) {
					read = channel.read (buffer, 0, bufferSize);
				}
				else {
					break;
				}
				double soundLevel = calculateLevel ();
				for (int i=0; i<silences-1; i++) {
					last [i] = last [i+1];
				}
				last [silences-1] = soundLevel;

				boolean silent = true;
				for (int i=0; i<silences; i++) {
					double x = last [i];
					if (x == huge) x = 1.0101;
					// System.out.format ("%.4f ", x);
					if (x != 0.0) silent = false;
				}
				// System.out.println ();

				if (silent) break;

				// will write only if we don't have
				// too many silent buffers
				if (read > 0) {
					byteOut.write (buffer, 0, read);
				}
			}
			channel.flush ();
			channel.stop ();
			int chop = chopSeconds (last);
			writeBytes (chop);
			parent.closechannel ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	double calculateLevel () {
		int best = 0;
		double max = (double)Short.MAX_VALUE;

		for (int i=0; i<bufferSize; i+=2) {
			int value = 0;
			// little endian
			int low = buffer [i];
			int high = buffer [i+1];
			value = (int)((short) ((short)high << 8) | (byte)low);
			if (value > best) best = value;
		}

		double level = (double)best/max;
		return level;
	}

	int chopSeconds (double last []) {
		// size of last is silences
		if (last.length != silences) {
			Log.severe ("array last is of size "+last.length+" should be "+silences);
			return 0;
		}
		// it is all the initial value of huge
		boolean ishuge = true;
		for (int i=0; i<silences; i++) {
			if (last [i] != huge) {
				ishuge = false;
				break;
			}
		}
		if (ishuge) return silences;

		// count how many zero seconds at the end
		int j = 0;
		for (int i=silences-1; i>=0; i--) {
			if (last [i] != 0.0) break;
			j++;
		}
		int chop = Math.max (0, j-2);
		return chop;
	}

	void writeBytes (int chop) {
		try {
			File F = new File (filename);
			byte data [] = byteOut.toByteArray ();
			int nframes = data.length/audioformat.getFrameSize ();
			int chopframes = chop * (int)audioformat.getSampleRate ();
			if (chopframes >= nframes) {
				Log.severe ("No audio detected");
				Log.severe ("Did not write "+filename);
				return;
			}
			nframes = nframes - chopframes;

			InputStream in = new ByteArrayInputStream (data);
			AudioInputStream incoming = new AudioInputStream (in, audioformat, nframes);
			AudioSystem.write (incoming, type, F);
			incoming.close ();
			Log.info ("Write "+filename+" "+nframes+" frames");
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

};

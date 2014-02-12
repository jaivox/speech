
package com.jaivox.util;

import java.io.File;
import java.util.ArrayList;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class WavReader {

	AudioFormat audioformat;
	AudioFileFormat.Type type;
	AudioInputStream in;
	int bufferSize;
	
	ArrayList <byte []> data;
	
	/**
	 * This version of the WavReader assumes that the file being read is a WAVE
	 * file, sample rate 44100, 16 bits, mono (1 channel), 2 bytes per sample.
	 */
	
	public WavReader () {
		try {
			type = AudioFileFormat.Type.WAVE;
			audioformat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				44100.0F, 16, 1, 2, 44100.0F, false);
			bufferSize = (int)(audioformat.getSampleRate () * audioformat.getFrameSize ());
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}
	
	public boolean initialzeFile (String wavfile) {
		try {
			File F = new File (wavfile);
			if (!F.exists ()) {
				Log.severe ("No audio file "+wavfile);
				return false;
			}
			in = AudioSystem.getAudioInputStream (F);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}
	
	public boolean closeAudioStream () {
		try {
			if (in == null) return false;
			in.close ();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}
	
	public int loadBuffers (int nBuffers) {
		try {
			if (data == null) {
				data = new ArrayList <byte []> ();
				for (int i=0; i<nBuffers; i++) {
					byte buffer [] = new byte [bufferSize];
					if (in.available () > 0) {
						int got = in.read (buffer, 0, bufferSize);
						data.add (buffer);
						if (got < bufferSize) {
							int nframes = i;
							return nframes;
						}
					}
					else return data.size ();
				}
				return nBuffers;
			}
			else {
				byte buffer [] = new byte [bufferSize];
				if (in.available () > 0) {
					int got = in.read (buffer, 0, bufferSize);
					if (data.size () > 0) {
						data.remove (0);
					}
					data.add (buffer);
					return data.size ();
				}
				else {
					if (data.size () > 0) {
						data.remove (0);
					}
					return data.size ();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
			return 0;
		}
	}
	
	public boolean writeBuffers (String filename) {
		try {
			int n = data.size ();
			if (n == 0) return false;
			byte bytes [] = new byte [n*bufferSize];
			for (int i=0; i<n; i++) {
				int start = i*bufferSize;
				byte buffer [] = data.get (i);
				for (int j=0; j<bufferSize; j++) {
					bytes [start+j] = buffer [j]; 
				}
			}
			com.jaivox.util.WavWriter writer = new com.jaivox.util.WavWriter ();
			writer.writeBytes (filename, bytes);
			Log.fine ("Wrote "+filename);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}
	
}

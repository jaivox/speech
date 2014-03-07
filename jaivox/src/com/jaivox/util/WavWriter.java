/*
   Jaivox version 0.7 March 2014
   Copyright 2010-2014 by Bits and Pixels, Inc.

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class WavWriter {
	AudioFormat audioformat;
	AudioFileFormat.Type type;
	
/**
 * This version writes WAVE files sample rate 44100, 16 bits, 1 channel,
 * 2 bytes per sample.
 */
	
	public WavWriter () {
		try {
			type = AudioFileFormat.Type.WAVE;
			audioformat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				44100.0F, 16, 1, 2, 44100.0F, false);
			int size = (int)(audioformat.getSampleRate () * audioformat.getFrameSize ());

		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

/**
 * Write a stream of raw bytes of data to a file
 * @param filename
 * @param data 
 */
	public void writeBytes (String filename, byte data []) {
		try {
			File F = new File (filename);
			int div = audioformat.getFrameSize ();
			int len = data.length;
			int nframes = len/div;
			InputStream in = new ByteArrayInputStream (data);
			AudioInputStream incoming = new AudioInputStream (in, audioformat, nframes);
			AudioSystem.write (incoming, type, F);
			incoming.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}
}

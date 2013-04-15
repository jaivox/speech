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
package com.jaivox.synthesizer.freetts;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import java.util.Properties;
// import com.sun.speech.freetts.audio.JavaClipAudioPlayer;

/**
 * The Synthesizer freetts class naturally needs the FreeTts Java library.
 * We have tested with version 1.2.2. Note that classpath should explicitly
 * contain the path to the actual jar file. For example
 * /home/you/yourfiles/freetts/freetts-1.2.2/lib/freetts.jar
 * where /home/you/yourfiles should be changed appropriately for
 * the location of the freetts files in your system.
 */

public class Synthesizer {

	VoiceManager tts;
	Voice speaker;

/**
 * Create a Synthesizer using information in a Properties class and
 * information about a base directory. This form is included only for
 * compatibility with other similar constructors in other packages.
@param base
@param kv
 */
	public Synthesizer (String base, Properties kv) {
		System.out.println ("Synthesizer created");
		initializeTts ();
	}

/**
 * Create the default synthesizer. This is the form that is most often
 * used to construct the Synthesizer class.
 */

	public Synthesizer () {
		System.out.println ("Default synthesizer created");
		initializeTts ();
	}

/**
 * Initialize the FreeTts text to speech system. This requires that
 * the path to freetts.jar (which contains other classes) should be part
 * of the classpath.
 */

	void initializeTts () {
		tts = VoiceManager.getInstance ();
        Voice [] voices = tts.getVoices ();
		if (voices == null || voices.length == 0) {
			System.out.println ("No voices available for freetts");
			System.exit (0);
		}
		System.out.println ("Available voices:");
        for (int i = 0; i < voices.length; i++) {
			String voice = voices [i].getName () + " " + voices [i].getDomain ();
			System.out.println (voice);
 		}

		speaker = tts.getVoice ("kevin16");
		speaker.allocate ();
	}


/**
 * speak the given message
@param message
 */
	public boolean speak (String message) {
		try {
			System.out.println ("Speaking: "+message);
			speaker.speak (message);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
	}
};


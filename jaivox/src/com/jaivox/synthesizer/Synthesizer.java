

package com.jaivox.synthesizer;

import java.util.Properties;


abstract public class Synthesizer {
	
	public Synthesizer (Properties kv) {
	}


	public Synthesizer () {
	}

	
	public boolean speak (String lang, String message) {
		return false;
	}
	
	public boolean speak (String message) {
		return false;
	}
	
	
}

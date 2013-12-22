/*
   Jaivox version 0.6 December 2013
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
package com.jaivox.synthesizer.espeak;

import com.jaivox.agent.MessageData;
import com.jaivox.agent.Responder;
import com.jaivox.agent.Session;
import java.util.Properties;

/**
 * The SynthServer creates an SynthResponder for each connection.
 * This responder uses an Synthesizer to speak according to messages sent to the
 * agent through the SynthServer. This class uses the espeak application
 * available on various platforms.
 */

public class SynthResponder extends Responder {

	public static final String
		terminateRequest = "JviaTerminate",
		uwhoRequest = "JviaWho",
		fetchRequest = "JviaFetch";

	static final int packetSize = 1024;
	Synthesizer Synth;
	Properties kv;

/**
 * The Synthesizer class may need information about directories, classpath
 * and others, this form of the constructor passes it along.
@param basedir
@param specfile
 */
	public SynthResponder (String basedir, Properties pp, Synthesizer I) {
		super ();
		kv = pp;
		Synth = I;
	}

/**
 * The default form of the SynthResponder constructor
 */

	public SynthResponder () {
		super ();
		Synth = new Synthesizer ();
	}

/**
 * If a session is already created for this Responder, this constructor
 * sets that information.
@param session
*/
	public SynthResponder (Session own) {
		super (own);
		Synth = new Synthesizer ();
	}

/**
 * responds to speak sent to an SynthServer agent.
 * The response generally just acknowledges that the message was spoken.
@param request	Request from another agent
 */

	public MessageData respond (String request) {
		MessageData req = new MessageData (request);
		MessageData jd = new MessageData ();
		String action = Session.responseMessage;
		jd.setValue ("action", action);
		String from = getOwner().getSid ();
		String to = "undetermined";
		if (!req.isValid ()) {
			action = Session.invalidMessage;
		}
		to = req.getValue ("from");
		action = req.getValue ("action");
		jd.setValue ("to", to);
		jd.setValue ("from", from);
		// String actreq = req.getValue ("message");
		if (action.equals ("speak")) {
			// speak using the attached Synthesizer
			String tospeak = req.getValue ("message");
			if (Synth.speak (tospeak)) {
				jd.setValue ("message", "\"spoke it\"");
				jd.setValue ("action", "spoken");
				return jd;
			}
			else {
				jd.setValue ("message", "\"TtsError\"");
				jd.setValue ("action", "error");
				return jd;
			}
		}
		else if (action.equals (terminateRequest)) {
			jd.setValue ("action", Session.terminateMessage);
		}
		else if (action.equals (uwhoRequest)) {
			jd.setValue ("action", Session.responseMessage);
			String s = "i am "+ getOwner ().getSid ();
			jd.setValue ("message", "\""+s+"\"" );
		}
		else if (action.equals (Session.responseMessage)) {
			jd.setValue ("action", Session.finishedMessage);
		}
		else {
			jd.setValue ("action", Session.invalidMessage);
		}
		return jd;
	}

}

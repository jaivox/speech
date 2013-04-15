package com.jaivox.synthesizer.freetts;

import com.jaivox.agent.*;
import java.util.Properties;

import com.jaivox.util.Log;

/**
 * The SynthServer creates an SynthResponder for each connection.
 * This responder uses an Synthesizer to speak according to messages sent to the
 * agent through the SynthServer.
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

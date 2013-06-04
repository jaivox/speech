
package com.jaivox.recognizer.web;

import com.jaivox.agent.MessageData;
import com.jaivox.agent.Responder;
import com.jaivox.agent.Session;

/**
 * This class is a simple responder, since the WebAsr agent mostly just
 * sends recognized strings to something else. It is not much different
 * from the agent.TestResponder. It is kept here since the SpeechSession
 * requires some resonder.
 */

public class WebAsrResponder extends Responder {

	public static final String
		terminateRequest = "JviaTerminate",
		uwhoRequest = "JviaWho",
		fetchRequest = "JviaFetch";
	
	static final int packetSize = 1024;
		
	public WebAsrResponder () {
		super ();
	}
	
	public WebAsrResponder (Session own) {
		super (own);
	}
	
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
		if (action.equals ("inform")) {
			System.out.println ("Received inform message, end of chain");
			System.out.println ("Informed: "+request);
			jd.setValue ("action", Session.finishedMessage);
		}
		else if (action.equals (terminateRequest)) {
			jd.setValue ("action", Session.terminateMessage);
		}
		else if (action.equals (uwhoRequest)) {
			jd.setValue ("action", Session.responseMessage);
			String s = "i am "+ getOwner ().getSid ();
			jd.setValue ("message", s);
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

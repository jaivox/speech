/**
 * TestResponder responds to messages in trivial ways. It is used
 * to test agents in terms of connectivity and a few simple requests.
 */

package com.jaivox.agent;

public class TestResponder extends Responder {

	public static final String
		terminateRequest = "JviaTerminate",
		uwhoRequest = "JviaWho",
		fetchRequest = "JviaFetch";
	
	static final int packetSize = 1024;
		
	public TestResponder () {
		super ();
	}
	
	public TestResponder (Session own) {
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
		if (action.equals (terminateRequest)) {
			jd.setValue ("action", Session.terminateMessage);
		}
		else if (action.equals (uwhoRequest)) {
			jd.setValue ("action", Session.responseMessage);
			String s = "i am "+getOwner ().getId ();
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

/*
   Jaivox version 0.3 December 2012
   Copyright 2010-2012 by Bits and Pixels, Inc.

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

package com.jaivox.interpreter;

import com.jaivox.agent.*;
import java.util.Properties;

import com.jaivox.util.Log;

/**
 * The InterServer creates an InterResponder for each connection.
 * This responder uses an Interact to interpret messages sent to the
 * agent through the InterServer.
 */

public class InterResponder extends Responder {

	public static final String
		terminateRequest = "JviaTerminate",
		uwhoRequest = "JviaWho",
		fetchRequest = "JviaFetch";
	
	static final int packetSize = 1024;
	Interact inter;
	Properties kv;
	
/**
 * The Interact class needs some data in a directory we call basedir.
 * In this form, the basedir and the important specifications file
 * are pssed to the Interact class.
@param basedir
@param specfile
 */
	public InterResponder (String basedir, Properties pp, Interact I) {
		super ();
		kv = pp;
		inter = I;
	}
		
	public InterResponder () {
		super ();
		inter = new Interact ();
	}
	
	public InterResponder (Session own) {
		super (own);
		inter = new Interact ();
	}
	
/**
 * responds to requests (in other words, questions) sent to an InterServer
 * agent. The response is in the form of a MessageData object which is
 * converted to a Json format string before it is sent via the agent's
 * socket connection.
@param request	Question from another agent, usually from speech recognizer
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
		if (action.equals ("interpret")) {
			MessageData reply = new MessageData (request);
			String question = req.getValue ("message");
			String response = inter.execute (question);
			if (!response.startsWith ("\"")) {
				response = "\""+response;
			}
			if (!response.endsWith ("\"")) {
				response = response+"\"";
			}
			reply.setValue ("message", response);
			reply.setValue ("action", "speak");
			String synthesizer = kv.getProperty ("synthesizer");
			InterSession mysession = (InterSession)getOwner ();
			InterSession tosynth = mysession.appSessionTo (synthesizer);
			if (tosynth == null) {
				Log.warning ("No session established to "+synthesizer);
				reply.setValue ("action", Session.invalidMessage);
			}
			else {
				reply.setValue ("from", kv.getProperty ("interpreter"));
				reply.setValue ("to", synthesizer);
			}
			return reply;
		}
		else if (action.equals ("spoken")) {
			// something can be sent back at this point if we want to
			// monitor the status of commands to speak something
			jd.setValue ("from", kv.getProperty ("interpreter"));
			jd.setValue ("to", kv.getProperty ("recognizer"));
			jd.setValue ("message", "\"spoke it\"");
			return jd;
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

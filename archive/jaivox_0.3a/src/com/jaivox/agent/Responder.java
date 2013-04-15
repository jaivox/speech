
package com.jaivox.agent;

/**
 * Connections between agents are set up as Sessions. Each Session has 
 * a Responder associated with it. The Responds handles the behavior
 * of the agent. It is often subclassed for each type of agent.
 */

public class Responder {

	Session owner;
	
	public Responder () {
		
	}
	
/**
 * Responders are usually created by Sessions
@param own	The session that "owns" this Responder
 */
	
	public Responder (Session own) {
		owner = own;
	}
	
	public Session getOwner () {
		return owner;
	}

	public void setOwner (Session own) {
		owner = own;
	}
	
/**
 * Create the MessageData associated with a request. The request is
 * a message that is received by the owning Session of this Responder.
 * Since this reponse depends on the application, this method is often
 * implemented in subclasses of the generic Responder	
@param request	String received by the session
@return	a MessageData object containing the response
 */
	public MessageData respond (String request) {
		MessageData req = new MessageData (request);
		MessageData jd = new MessageData ();
		String action = Session.responseMessage;
		String from = owner.sid;
		String session = "unknown";
		String to = "undetermined";
		if (!req.isValid ()) {
			action = Session.invalidMessage;
		}
		else {
			to = req.getValue ("from");
			session = req.getValue ("session");
		}
		jd.createKeyValues (action, from, to, session);
		return jd;
	}
	
/**
 * String form of respond (). This version converts the MessageData
 * into a String and returns the String (generally in a Json format
 * message handed by MessageData.)
@param request String received by the owning session
@return	message to be sent back in response
 */
	public String responseString (String request) {
		MessageData jd = respond (request);
		String result = jd.createMessage ();
		return result;
	}
	
}

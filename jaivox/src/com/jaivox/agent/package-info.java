
/**
The agent package is for creating agents, i.e. independent programs that
may run anywhere in the network. Jaivox uses these to place any of the
different functions within Jaivox anywhere in a network, or within the
same computer. The use of agents in the same computer helps keep different
functions separate. The only communication between, for example, a speech
recognizer and an interpreter will be through sockets. The Java virtual
machine of one will not affect the other, helping reduce both memory
load on a single virtual machine and also helping isolate bugs.

In this implementation, each agent extends Server. A connection between
Servers is managed by a Session. When a Server receives a message through
a Session, it uses a Responder to understand the message and to produce
a response.

TestServer is an example of a Server. It has a command line interface to
allow users to enter commands to the Server. It uses TestResponder to
produce some simple responses to certain queries.

The messages sent between Servers is parsed and assembled using the
MessageData class. The messages are in a simplified form of the Json
format. MessageData handles this simplified format. If you need to use
arbitrarily complex Json messages, it is better to use another full-scale
Json implementation.
*/
package com.jaivox.agent;

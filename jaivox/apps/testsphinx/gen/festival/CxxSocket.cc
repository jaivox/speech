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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <sys/types.h>			// types in socket.h
#include <sys/socket.h>			// socket definitions
#include <resolv.h>
#include <netinet/in.h>			// internet constants
#include <netdb.h>				// for struct hostent
#include <unistd.h>				// for gethostname

#include "CxxSocket.h"

CxxSocket::CxxSocket (char* host, int port) {
    Host = host;
    Port = port;
}

CxxSocket::CxxSocket (char* host, int port, int connection) {
    Host = host;
    Port = port;
    Sock = connection;
	fprintf (stdout, "CxxSocket: connected to %s at port %d\n", host, port);
}

CxxSocket::CxxSocket () {
}

int CxxSocket::socketStart () {
    return 1;
}

int CxxSocket::openSocket (char* host, int port) {

    Host = host;
    Port = port;
    fprintf (stdout, "Opening socket Host %s Port %d\n", Host, Port);

    if (!socketStart ())
        return 0;

    Sock = socket (AF_INET, SOCK_STREAM, 0);

    if (Sock < 0) {
        fprintf (stdout, "CxxSocket: Cannot open socket to Host %s Port %d\n", Host, Port);
        return 0;
    }

    server = gethostbyname (Host);
    if (server == (struct hostent*) NULL) {
        fprintf (stdout, "CxxSocket: Cannot get host %s\n", Host);
        return 0;
    }

    // Prepare addr record before making connect request
    addr.sin_family = AF_INET;
    int tryConnect;

    addr.sin_port = htons (port);
    bcopy ((char*) server->h_addr, (char*) & addr.sin_addr, server->h_length);
    tryConnect = connect (Sock, (struct sockaddr*) & addr, sizeof (addr));

    if (tryConnect < 0) {
        fprintf (stdout, "CxxSocket: Cananot connect to Host %s Port %d\n", Host, Port);
        closeSocket ();
        return 0;
    }

    return 1;
}

int CxxSocket::closeSocket () {

    // Info message
    fprintf (stdout, "%s:%d Attempting to close socket\n", Host, Port);

    if (shutdown (Sock, SHUT_RDWR) == SOCKET_ERROR) {
        fprintf (stdout, "CxxSocket: %s:%d Socket error sending before closing\n", Host, Port);
        return 0;
    }

    if (close (Sock) == SOCKET_ERROR) {
        fprintf (stdout, "CxxSocket: %s:%d Socket error in closing\n", Host, Port);
        return 0;
    }

    // Info message
    fprintf (stdout, "%s:%d Socket closed\n", Host, Port);

    return 1;
}

int CxxSocket::getData (char* buffer, int bufferSize) {

    int BytesRead = recv (Sock, buffer, bufferSize, 0);

    if (BytesRead == 0) {
        fprintf (stdout, "CxxSocket: Reading: Connection to %s:%d closed by peer\n",
                 Host, Port);
        closeSocket ();
        return 0;
    }

    if (BytesRead == SOCKET_ERROR) {
        fprintf (stdout, "CxxSocket: %s:%d Reading error receiving data\n",
                 Host, Port);
        closeSocket ();
        return 0;
    }

    buffer [BytesRead] = '\0';
    // buffer usually has \n in it
    fprintf (stdout, "%s:%d Read [%d]: %s\n", Host, Port, BytesRead, buffer);

    return BytesRead;
}

int CxxSocket::sendData (char* buffer, int maxChar) {

    int toSend = strlen (buffer);

    if (toSend <= 0 || toSend > maxChar) {
        fprintf (stdout, "CxxSocket: %s:%d Malformed info %s to send: closing socket\n",
                 Host, Port, buffer);
        closeSocket ();
        return 0;
    }

    int BytesSent = send (Sock, buffer, toSend, 0);
    if (BytesSent == 0) {
        fprintf (stdout, "CxxSocket: Sending: Connection to %s:%d closed by peer\n",
                 Host, Port);
        closeSocket ();
        return 0;
    }

    if (BytesSent == SOCKET_ERROR) {
        fprintf (stdout, "CxxSocket: %s:%d Sending error receiving data\n",
                 Host, Port);
        closeSocket ();
        return 0;
    }

    // buffer has \r\n in it
    fprintf (stdout, "%s:%d Sent [%d]: %s\n", Host, Port, BytesSent, buffer);

    return BytesSent;
}

CxxServerSocket::CxxServerSocket (char* host, int port) : CxxSocket (host, port) {

}

int CxxServerSocket::Create () {
    if (!socketStart ())
        return 0;

    Sock = socket (AF_INET, SOCK_STREAM, 0);

    if (Sock < 0) {
        fprintf (stdout, "iaServerSocket: Cannot open socket to Host %s Port %d\n", Host, Port);
        return 0;
    }

    addr.sin_family = PF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = htons (Port);

    if (bind (Sock, (struct sockaddr *) & addr, sizeof (addr)) < 0) {
        fprintf (stdout, "iaServerSocket: %s:%d Cannot bind to socket %d\n",
                 Host, Port, Sock);
        closeSocket ();
        return 0;
    }

    // Info message
    fprintf (stdout, "iaServerSocket: %s:%d Listening for connect requests\n",
             Host, Port);

    listen (Sock, 10);
    return 1;
}

CxxSocket* CxxServerSocket::Accept () {

    socklen_t i = sizeof (caller);
    int conn;

    if ((conn = accept (Sock, (struct sockaddr *)&caller, &i)) < 0) {
        fprintf (stdout, "iaServerSocket: %s:%d Can not accept connection request\n",
                 Host, Port);
        if (closeSocket () == SOCKET_ERROR) {
            fprintf (stdout, "iaServerSocket: %s:%d Socket error in closing\n", Host, Port);
        }
        // Info message
        fprintf (stdout, "iaServerSocket: %s:%d Socket closed\n", Host, Port);
        return NULL;
    }

    char req [256];
    gethostname (req, 256);
    int clientPort = ntohs (caller.sin_port);
    // Info message
    fprintf (stdout, "iaServerSocket: %s:%d Connection made from %s port %d\n",
             Host, Port, req, clientPort);

    char* remoteHost = new char [strlen (req) + 1];
    strcpy (remoteHost, req);

    CxxSocket* Client = new CxxSocket (remoteHost, clientPort, conn);

    return Client;
}


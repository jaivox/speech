/*
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

#ifndef _CxxSocketDefined
#define _CxxSocketDefined

typedef int SOCKET;
#define INVALID_SOCKET (SOCKET)(~0)
#define SOCKET_ERROR (-1)

class CxxSocket {
public:
    CxxSocket (char* host, int port);
    CxxSocket (char* host, int port, int connection);
    CxxSocket ();

    int openSocket (char* host, int port);
    int closeSocket ();

    int sendData (char* buffer, int maxChar);
    int getData (char* buffer, int bufferSize);

    int socketStart ();

    char* Host;
    int Port;
    SOCKET Sock;
    struct sockaddr_in addr;
    struct hostent *server;

};

class CxxServerSocket : public CxxSocket {
public:
    CxxServerSocket (char* host, int port);

    int Create ();
    CxxSocket* Accept ();

private:
    struct sockaddr_in caller;

};


#endif


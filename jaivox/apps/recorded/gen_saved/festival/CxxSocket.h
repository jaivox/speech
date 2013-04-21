
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


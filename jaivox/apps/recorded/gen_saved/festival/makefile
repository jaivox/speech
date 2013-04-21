# makefile for Cxx with festival

FLAGS = -I/usr/local/include -I/usr/include/speech_tools -I/usr/include/festival -DLINUX -g
LIBS = -lpthread -lFestival -leststring -lestools -lestbase

OBJS = CxxServer.o CxxSession.o CxxResponder.o CxxSocket.o CxxThread.o CxxData.o

festivaltest: $(OBJS)
	g++ -o$@ $(OBJS) $(LIBS)

.cc.o:
	g++ $(FLAGS) -c $<


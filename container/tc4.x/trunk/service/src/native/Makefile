include ../Makedefs

OBJS = jsvc_help.o jsvc_parse.o jsvc_service.o

all: jsvc

clean:
	rm -f $(OBJS) jsvc 

jsvc: jsvc_unix.c $(OBJS)
	$(CC) $(COPTS) $(CINCS) $(CLIBS) $(OBJS) jsvc_unix.c -o jsvc

.c.o:
	$(CC) $(COPTS) $(CINCS) -c $< -o $@

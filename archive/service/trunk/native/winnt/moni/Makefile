INCLUDE=-I../lib
all: ../bin/vdmoniadm ../bin/vdmonisvc

../bin/vdmoniadm: vdmoniadm.c
	gcc $(INCLUDE) -I../executables/vdmoniadm  vdmoniadm.c \
	-o ../bin/vdmoniadm -lgdi32
../bin/vdmonisvc: vdmonisvc.c vdenv.o kills.o
	gcc -mno-cygwin -I/usr/include $(INCLUDE) -I../executables/vdmonisvc \
	-DCYGWIN vdmonisvc.c \
	-o ../bin/vdmonisvc vdenv.o kills.o
vdenv.o: ../supcalls_nt/vdenv.c
	gcc $(INCLUDE) -c ../supcalls_nt/vdenv.c
kills.o: ../signals/kills.c
	gcc $(INCLUDE) -c ../signals/kills.c

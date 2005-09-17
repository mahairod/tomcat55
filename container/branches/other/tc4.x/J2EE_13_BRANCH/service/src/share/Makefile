include ../Makedefs

PACKAGES = org.apache.service org.apache.service.helpers

all: service.jar

service.jar: ../../build
	SOURCES=`find . -type f | grep \\.java | sort` ; \
	$(JAVAC) $(JAVACOPTS) -sourcepath . -d ../../build $$SOURCES ; \
	CURDIR=$(PWD) ; \
	cd ../../build ; \
	$(JAR) -cvf0 $$CURDIR/service.jar * ; \
	cd $$CURDIR
	
javadocs: ../../javadocs
	$(JAVADOC) $(JAVADOCOPTS) -d ../../javadocs $(PACKAGES)
clean:
	rm -rf ../../build
	rm -f service.jar

../../build:
	mkdir ../../build

../../javadocs:
	mkdir ../../javadocs

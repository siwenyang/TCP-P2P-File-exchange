JAVAC=javac
sources = $(wildcard *.java)
classes = $(sources:.java=.class)

all: $(classes)
	chmod a+x client
	chmod a+x server

clean :
	rm -f *.class
	rm -f port

%.class : %.java
	$(JAVAC) $<

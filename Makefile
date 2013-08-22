JAVAC = javac -O

DEST  = classes

all: jar tutorial

jar: languages
	$(MAKE) -C lib

languages: source
	$(MAKE) -C languages

source:
	$(MAKE) -C src
    
tutorial:
	$(MAKE) -C doc/tutorial

regression:
	$(MAKE) -C languages regression

clean:
	$(MAKE) -C lib clean


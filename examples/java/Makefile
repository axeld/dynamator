DYNAMATE = java dynamate -d generated
JAVAC = javac

all: \
    generated/HelloWorld.class \
    generated/FontSizes.class \
    generated/TheTime.class \
    generated/TermsDictionary.class \
    generated/TermsTable.class \
    generated/Calendar.class \
    generated/Taxonomy.class

generated/%.class: %.java
	$(JAVAC) -d generated $<

generated/HelloWorld.java: ../HelloWorld.html HelloWorld.dyn
	$(DYNAMATE) ../HelloWorld.html

generated/FontSizes.java: ../FontSizes.html FontSizes.dyn
	$(DYNAMATE) ../FontSizes.html

generated/TheTime.java: ../TheTime.html TheTime.dyn
	$(DYNAMATE) ../TheTime.html

generated/TermsDictionary.java: ../TermsDictionary.html TermsDictionary.dyn
	$(DYNAMATE) ../TermsDictionary.html

generated/TermsTable.java: ../TermsTable.html TermsTable.dyn
	$(DYNAMATE) ../TermsTable.html

generated/Calendar.java: ../Calendar.html Calendar.dyn
	$(DYNAMATE) ../Calendar.html

generated/Taxonomy.java: ../Taxonomy.html Taxonomy.dyn
	$(DYNAMATE) ../Taxonomy.html


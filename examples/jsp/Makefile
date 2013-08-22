DYNAMATE = java dynamate -d generated

all: HelloWorld.jsp FontSizes.jsp TheTime.jsp TermsTable.jsp \
    TermsDictionary.jsp \
    Snoop.jsp Threads.jsp GuessANumber.jsp TextEcho3.jsp \
    Calendar.jsp Taxonomy.jsp

HelloWorld.jsp: ../HelloWorld.html HelloWorld.dyn
	$(DYNAMATE) ../HelloWorld.html

FontSizes.jsp: ../FontSizes.html FontSizes.dyn
	$(DYNAMATE) ../FontSizes.html

TermsTable.jsp: ../TermsTable.html TermsTable.dyn
	$(DYNAMATE) ../TermsTable.html

TermsDictionary.jsp: ../TermsDictionary.html TermsDictionary.dyn
	$(DYNAMATE) ../TermsDictionary.html

Snoop.jsp: Snoop.html Snoop.dyn
	$(DYNAMATE) Snoop.html
    
TheTime.jsp: TheTime.html TheTime.dyn
	$(DYNAMATE) TheTime.html
    
Threads.jsp: Threads.html Threads.dyn
	$(DYNAMATE) Threads.html
    
GuessANumber.jsp: GuessANumber.html GuessANumber.dyn
	$(DYNAMATE) GuessANumber.html
    
TextEcho3.jsp: TextEcho3.html TextEcho3.dyn
	$(DYNAMATE) TextEcho3.html
    
Calendar.jsp: Calendar.html Calendar.dyn
	$(DYNAMATE) Calendar.html
    
Taxonomy.jsp: Taxonomy.html Taxonomy.dyn
	$(DYNAMATE) Taxonomy.html

clean_dos:
	del generated/*
    
clean_unix:
	rm generated/*


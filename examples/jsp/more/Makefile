DYNAMATE = java dynamate -d generated

all: \
    generated/Snoop.jsp \
    generated/Threads.jsp \
    generated/GuessANumber.jsp \
    generated/TextEcho3.jsp \
    generated/Taxonomy.jsp

generated/Snoop.jsp: Snoop.html Snoop.dyn
	$(DYNAMATE) Snoop.html
    
generated/Threads.jsp: Threads.html Threads.dyn
	$(DYNAMATE) Threads.html
    
generated/GuessANumber.jsp: GuessANumber.html GuessANumber.dyn
	$(DYNAMATE) GuessANumber.html
    
generated/TextEcho3.jsp: TextEcho3.html TextEcho3.dyn
	$(DYNAMATE) TextEcho3.html

generated/Taxonomy.jsp: Taxonomy.html Taxonomy.dyn
	$(DYNAMATE) Taxonomy.html

clean_dos:
	del generated/*
	del *.asxml

clean_unix:
	rm generated/*
	rm *.asxml


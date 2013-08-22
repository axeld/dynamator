DYNAMATE = java dynamate -d generated

all: \
    generated/HelloWorld.php \
    generated/FontSizes.php \
    generated/TheTime.php \
    generated/TermsTable.php \
    generated/TermsDictionary.php \
    generated/Calendar.php \
    generated/Taxonomy.php

generated/HelloWorld.php: HelloWorld.dyn ../HelloWorld.html
	$(DYNAMATE) ../HelloWorld.html

generated/FontSizes.php: FontSizes.dyn ../FontSizes.html
	$(DYNAMATE) ../FontSizes.html

generated/TheTime.php: TheTime.dyn ../TheTime.html
	$(DYNAMATE) ../TheTime.html

generated/TermsTable.php: TermsTable.dyn ../TermsTable.html
	$(DYNAMATE) ../TermsTable.html

generated/TermsDictionary.php: TermsDictionary.dyn ../TermsDictionary.html
	$(DYNAMATE) ../TermsDictionary.html

generated/Calendar.php: Calendar.dyn ../Calendar.html
	$(DYNAMATE) ../Calendar.html

generated/Taxonomy.php: Taxonomy.dyn ../Taxonomy.html
	$(DYNAMATE) ../Taxonomy.html

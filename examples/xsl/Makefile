all: \
    generated/books.xsl \
    generated/poem.xsl \
    generated/FontSizes.xsl \
    generated/HelloWorld.xsl \
    generated/TheTime.xsl \
    generated/TermsTable.xsl \
    generated/TermsDictionary.xsl \
    generated/Taxonomy.xsl

generated/books.xsl: books.html books.dyn
	java dynamate -d generated -X books.html
    
generated/poem.xsl: poem.html poem.dyn
	java dynamate -d generated -X poem.html
    
generated/FontSizes.xsl: ../FontSizes.html FontSizes.dyn
	java dynamate -d generated -X ../FontSizes.html

generated/HelloWorld.xsl: ../HelloWorld.html HelloWorld.dyn
	java dynamate -d generated -X ../HelloWorld.html

generated/TheTime.xsl: ../TheTime.html TheTime.dyn
	java dynamate -d generated -X ../TheTime.html

generated/TermsTable.xsl: ../TermsTable.html TermsTable.dyn
	java dynamate -d generated -X ../TermsTable.html

generated/TermsDictionary.xsl: ../TermsDictionary.html TermsDictionary.dyn
	java dynamate -d generated -X ../TermsDictionary.html

generated/Taxonomy.xsl: ../Taxonomy.html Taxonomy.dyn
	java dynamate -d generated -X ../Taxonomy.html

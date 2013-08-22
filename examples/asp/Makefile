all: \
    generated/HelloWorld.asp \
    generated/FontSizes.asp \
    generated/TheTime.asp \
    generated/TermsTable.asp \
    generated/TermsDictionary.asp

generated/HelloWorld.asp: HelloWorld.dyn ../HelloWorld.html
	java dynamate -d generated ../HelloWorld.html

generated/FontSizes.asp: FontSizes.dyn ../FontSizes.html
	java dynamate -d generated ../FontSizes.html

generated/TheTime.asp: TheTime.dyn ../TheTime.html
	java dynamate -d generated ../TheTime.html

generated/TermsTable.asp: TermsTable.dyn ../TermsTable.html
	java dynamate -d generated ../TermsTable.html

generated/TermsDictionary.asp: TermsDictionary.dyn ../TermsDictionary.html
	java dynamate -d generated ../TermsDictionary.html


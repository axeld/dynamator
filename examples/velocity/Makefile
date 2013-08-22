all: \
    generated/HelloWorld.vm \
    generated/FontSizes.vm \
    generated/TheTime.vm \
    generated/TermsTable.vm \
    generated/TermsDictionary.vm


generated/HelloWorld.vm: HelloWorld.dyn ../HelloWorld.html
	java dynamate -d generated ../HelloWorld.html

generated/FontSizes.vm: FontSizes.dyn ../FontSizes.html
	java dynamate -d generated ../FontSizes.html

generated/TheTime.vm: TheTime.dyn ../TheTime.html
	java dynamate -d generated ../TheTime.html

generated/TermsTable.vm: TermsTable.dyn ../TermsTable.html
	java dynamate -d generated ../TermsTable.html

generated/TermsDictionary.vm: TermsDictionary.dyn ../TermsDictionary.html
	java dynamate -d generated ../TermsDictionary.html

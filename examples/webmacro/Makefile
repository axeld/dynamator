all: \
    generated/HelloWorld.wm \
    generated/FontSizes.wm \
    generated/TheTime.wm \
    generated/TermsTable.wm \
    generated/TermsDictionary.wm


generated/HelloWorld.wm: HelloWorld.dyn ../HelloWorld.html
	java dynamate -d generated ../HelloWorld.html

generated/FontSizes.wm: FontSizes.dyn ../FontSizes.html
	java dynamate -d generated ../FontSizes.html

generated/TheTime.wm: TheTime.dyn ../TheTime.html
	java dynamate -d generated ../TheTime.html

generated/TermsTable.wm: TermsTable.dyn ../TermsTable.html
	java dynamate -d generated ../TermsTable.html

generated/TermsDictionary.wm: TermsDictionary.dyn ../TermsDictionary.html
	java dynamate -d generated ../TermsDictionary.html

all: html/HelloWorld.html html/HelloImage.html htdocs/HelloUser.jsp

# $@ is replaced by the target,
# so ../../$@ == ../../html/%.html == ../%.html

html/%.html: dyn/%.dyn ../%.html
	java dynamate -f dyn -d html ../../$@

htdocs/%.jsp: html/%.html ../../dyn/%.dyn
	java dynamate -f ../../dyn -d htdocs -G -I . $<

.PRECIOUS: html/%.html

#-----------------------------------------------------------------
# This is how you would create the .dyn files using the
# English to Pig Latin translator:
#
# XSL=saxon
# dyn/%.dyn:
# 	$(XSL) ../en_us/$@ DynToPigLatin.xsl > $@
# 
# .PRECIOUS: dyn/%.dyn

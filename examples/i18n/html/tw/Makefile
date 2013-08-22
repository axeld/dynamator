all: htdocs/HelloUser.jsp

# $@ is replaced by the target,
# so ../../$@ == ../../html/%.html == ../%.html

html/%.html: dyn/%.dyn ../%.html
	java dynamate -e big5 -f dyn -d html ../../$@
    
htdocs/%.jsp: html/%.html ../../dyn/%.dyn
	java dynamate -e big5 -f ../../dyn -d htdocs -G -I . $<

.PRECIOUS: html/%.html

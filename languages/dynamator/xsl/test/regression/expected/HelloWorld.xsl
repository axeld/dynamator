<?xml version="1.0"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" >

<xsl:output method="html"/>

<xsl:template match="/">
<xsl:text disable-output-escaping="yes"><![CDATA[<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">]]></xsl:text><html>
  <head>
    <title>Hello World</title>
  </head>
  <body>
    <p id="DynamicText"><xsl:value-of select="//greeting"/></p>
  </body>
</html>

</xsl:template>
</xsl:stylesheet>
<!-- generated by Dynamator Mon Mar 29 22:38:43 CST 2004 -->

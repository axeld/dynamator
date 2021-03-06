<?xml version="1.0"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" >

<xsl:template match="/">
<xsl:text disable-output-escaping="yes"><![CDATA[<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">]]></xsl:text><html>
  <head>
    <title>What time is it?</title>
  </head>
  <body>
    <p>The time is <span id="CurrentTime"><xsl:value-of select="//time/@month"/> 
    <xsl:text> </xsl:text>
    <xsl:value-of select="//time/@day"/>,
    <xsl:value-of select="//time/@year"/>
    at
    <xsl:value-of select="//time/@hour"/>
    :
    <xsl:value-of select="//time/@minute"/></span></p>
    <xsl:if test="//time/@hour &lt; 12">
      <p id="IfMorning">Good Morning!</p>
    </xsl:if><xsl:if test="12 &lt;= //time/@hour and //time/@hour &lt; 18">
      <p id="IfAfternoon">Good Afternoon!</p>
    </xsl:if><xsl:if test="18 &lt;= //time/@hour">
      <p id="IfEvening">Good Evening!</p>
    </xsl:if>
  </body>
</html>

</xsl:template>
</xsl:stylesheet>
<!-- generated by Dynamator Mon Mar 29 22:38:48 CST 2004 -->

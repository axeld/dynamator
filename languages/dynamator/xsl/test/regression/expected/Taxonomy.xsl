<?xml version="1.0"?>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.1" >

<xsl:output method="html"/>

<xsl:template match="/">
<xsl:text disable-output-escaping="yes"><![CDATA[<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">]]></xsl:text><html>
  <head>
    <title>Taxonomy</title>
  </head>
  <body>
    <h1 id="title">An Informal Taxonomy of the Animal Kingdom</h1>
    <p id="root"><xsl:value-of select="/taxonomy/item/@name"/></p>

      <xsl:apply-templates select="/taxonomy/item" />
      </body>
</html>

</xsl:template>

<xsl:template match="item">
        <xsl:if test="item">
      <ul class="children-list">
        <xsl:for-each select="./*">
          <li class="child"><span class="child-name"><xsl:value-of select="@name"/></span> 

      <xsl:apply-templates select="item" />
              </li>
        </xsl:for-each>
      </ul>
    </xsl:if>

</xsl:template>
    <!-- generated by Dynamator Mon Mar 29 22:38:45 CST 2004 -->

</xsl:stylesheet>
<!-- generated by Dynamator Mon Mar 29 22:38:45 CST 2004 -->

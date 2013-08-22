<xsl:stylesheet 
    version="1.1"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns="http://www.w3.org/1999/xhtml"
    >
<xsl:param name="insert-from-dir"/>
<xsl:param name="nav-dir-name"/>
<xsl:param name="nav-index-document"/>

<xsl:variable name="nav-prev">
  <xsl:value-of select="document($nav-index-document)/tutorial/dir[@name=$nav-dir-name]/preceding-sibling::dir[1]/@name"/>
</xsl:variable>

<xsl:variable name="nav-next">
  <xsl:value-of select="document($nav-index-document)/tutorial/dir[@name=$nav-dir-name]/following-sibling::dir/@name"/>
</xsl:variable>

<xsl:template match="xhtml:div[@class = 'insert']">
  <xsl:variable name="file" select="text()"/>
  <xsl:copy-of select="document(concat($insert-from-dir,'/',$file,'.insert'))"/>
</xsl:template>

</xsl:stylesheet>
<?xml version="1.0"?>
<!--
  ============================================================
  Translate an English-language Dynamator file into Pig Latin.
  Only content entries are translated.
  Pig Latin dialect may not match your dialect.
  Don't expect perfection.
  The purpose of this script is to provide an easy way to test
  internationalized HTML pages without having to wait for localized
  copies.
  ============================================================
-->  
<xsl:stylesheet 
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="content">
  <xsl:copy>
    <xsl:apply-templates mode="content" select="node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="raw-content">
  <xsl:copy>
    <xsl:apply-templates mode="content" select="node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template mode="content" match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates mode="content" select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template mode="content" match="text()">
  <xsl:param name="text" select="concat(normalize-space(.), ' ')"/>
  <xsl:call-template name="replace">
    <xsl:with-param name="word" select="substring-before($text, ' ')"/>
    <xsl:with-param name="rest"  select="substring-after($text, ' ')"/>
  </xsl:call-template>     
</xsl:template> 

<xsl:variable name="consonantgroup" 
  select="xx|qu|st|th|br|bl|wh|tr|dr|cr|pr|pl|cl|mn|sc|gr|gl|kl|xx"/>
<xsl:variable name="capconsonantgroup" 
  select="xx|Qu|St|Th|Br|Bl|Wh|Tr|Dr|Cr|Pr|Pl|Cl|Mn|Sc|Gr|Gl|Kl|xx"/>

<xsl:template name="replace">
  <xsl:param name="word"/>
  <xsl:param name="rest"/>
  <xsl:variable name="lastchar" select="substring($word, string-length($word))"/>
  <xsl:choose>
    <xsl:when test="translate($lastchar, 
        'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
        '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~')=$lastchar">
      <xsl:call-template name="piggify">
        <xsl:with-param name="word" select="substring($word, 1, string-length($word)-1)"/>
        <xsl:with-param name="punct" select="$lastchar"/>
        <xsl:with-param name="rest" select="$rest"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="piggify">
        <xsl:with-param name="word" select="$word"/>
        <xsl:with-param name="rest" select="$rest"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="piggify">
  <xsl:param name="word"/>
  <xsl:param name="punct"/>
  <xsl:param name="rest"/>
  <xsl:variable name="char1" select="substring($word, 1, 1)"/>
  <xsl:choose>
    <xsl:when test="translate($char1, 
        'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
        '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~')=$char1">
      <xsl:value-of select="$word"/>
    </xsl:when>
    <xsl:when test="translate($char1, 'aeiouAEIOU', '~~~~~~~~~~')=$char1">
      <xsl:variable name="firsttwo" select="substring($word, 1, 2)"/>
      <xsl:variable name="firsttwosearch" select="concat('|',$firsttwo,'|')"/>
      <xsl:choose>
        <xsl:when test="substring-before($consonantgroup, $firsttwosearch)!=''">
          <xsl:call-template name="consonant-first">
            <xsl:with-param name="one" select="substring($word, 1, 1)"/>
            <xsl:with-param name="two" select="substring($word, 2, 1)"/>
            <xsl:with-param name="rest" select="substring($word, 3)"/>
          </xsl:call-template>          
        </xsl:when>
        <xsl:when test="substring-before($capconsonantgroup, $firsttwosearch)!=''">
          <xsl:call-template name="consonant-first">
            <xsl:with-param name="one" select="substring($word, 1, 1)"/>
            <xsl:with-param name="two" select="substring($word, 2, 1)"/>
            <xsl:with-param name="rest" select="substring($word, 3)"/>
          </xsl:call-template>          
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="consonant-first">
            <xsl:with-param name="one" select="substring($word, 1, 1)"/>
            <xsl:with-param name="rest" select="substring($word, 2)"/>
          </xsl:call-template>          
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="vowel-first">
        <xsl:with-param name="word" select="$word"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>

  <xsl:value-of select="$punct"/>
  
  <xsl:if test="$rest != ''">
    <xsl:text> </xsl:text>
    <xsl:call-template name="replace">
      <xsl:with-param name="word" select="substring-before($rest, ' ')"/>
      <xsl:with-param name="rest" select="substring-after($rest, ' ')"/>
    </xsl:call-template>      
  </xsl:if>

</xsl:template>

<xsl:template name="consonant-first">
  <xsl:param name="one"/>
  <xsl:param name="two"/>
  <xsl:param name="rest"/>

  <xsl:variable name="lcone" 
    select="translate($one, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>

  <xsl:choose>
    <xsl:when test="$lcone != $one">
      <xsl:variable name="r1" 
        select="translate(substring($rest, 1, 1),
        'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
      <xsl:variable name="r2" select="substring($rest, 2)"/>
      <xsl:call-template name="consonant-first">
        <xsl:with-param name="one" select="$lcone"/>
        <xsl:with-param name="two" select="$two"/>
        <xsl:with-param name="rest" select="concat($r1, $r2)"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="concat($rest, $one, $two, 'ay')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="vowel-first">
  <xsl:param name="word"/>
  <xsl:value-of select="$word"/>
  <xsl:text>way</xsl:text>
</xsl:template>


</xsl:stylesheet>
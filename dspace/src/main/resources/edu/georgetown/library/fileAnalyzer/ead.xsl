<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" version="1.0"
    xmlns:ead="urn:isbn:1-931666-22-9">
    <xsl:output method="text"/>
    <xsl:param name="collection"></xsl:param>
    <xsl:param name="rights">All Rights Reserved by Georgetown University Library.</xsl:param>
    <xsl:template match="/">
        <xsl:text>id</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>collection</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>dc.title[en]</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>dc.coverage.temporal[en]</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>dc.description[en]</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>dc.date.created[en]</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>dc.relation.isPartOf[en]</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>dc.rights[en]</xsl:text>
        <xsl:text>&#10;</xsl:text>
        
        <xsl:apply-templates select="//ead:c"/>
    </xsl:template>
    
    <xsl:template match="ead:c">
        <xsl:text>&quot;</xsl:text>
        <xsl:text>+</xsl:text>
        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="$collection"/>
        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select=".//ead:unittitle"/>
        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select=".//ead:unitdate"/>
        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="normalize-space(translate(.//ead:physdesc,'&quot;',''))"/>
        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:choose>
            <xsl:when test="contains(.//ead:unitdate,'-')">
                <xsl:value-of select="substring-before(.//ead:unitdate,'-')"/>
            </xsl:when>
            <xsl:when test="string-length(.//ead:unitdate)=0">
                <xsl:text>No Date</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select=".//ead:unitdate"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="//ead:titlestmt/ead:titleproper[1]"/>
        <xsl:for-each select=".//ead:container">
            <xsl:value-of select="concat('||',@type, ' ', text())"/>
        </xsl:for-each>
        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="$rights"/>
        <xsl:text>&quot;</xsl:text>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" version="1.0"
    xmlns:ead="urn:isbn:1-931666-22-9">
    <xsl:output method="text"/>
    <xsl:param name="collection"></xsl:param>
    <xsl:param name="rights">All Rights Reserved by Georgetown University Library.</xsl:param>
    <xsl:param name="refcol">gu.archivesspace.id</xsl:param>
    <xsl:template match="/">
        <xsl:text>id</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>collection</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:value-of select="$refcol"/>
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
        <xsl:text>,</xsl:text>
        <xsl:text>dc.language[en]</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>dc.contributor[en]</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>dc.subject[en]</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>dc.creator[en]</xsl:text>
        <xsl:text>&#10;</xsl:text>
        
        <xsl:apply-templates select="//ead:c|//ead:c01|//ead:c02|//ead:c03"/>
    </xsl:template>
    
    <xsl:template match="ead:c|ead:c01|ead:c02|ead:c03">

        <xsl:text>&quot;</xsl:text>
        <xsl:text>+</xsl:text>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="$collection"/>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:choose>
            <xsl:when test="starts-with(@id, 'aspace_')">
                <xsl:value-of select="substring(@id, 8)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@id"/>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select=".//ead:unittitle"/>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select=".//ead:unitdate"/>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="normalize-space(translate(.//ead:physdesc,'&quot;',''))"/>
        <xsl:value-of select="normalize-space(translate(.//ead:scopecontent/ead:p,'&quot;',''))"/>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:choose>
            <xsl:when test="contains(.//ead:unitdate/@normal,'/')">
                <xsl:value-of select="substring-before(.//ead:unitdate/@normal,'/')"/>
            </xsl:when>
            <xsl:when test=".//ead:unitdate/@normal">
                <xsl:value-of select=".//ead:unitdate/@normal"/>
            </xsl:when>
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
        <xsl:value-of select="concat(//ead:archdesc//ead:unittitle, ' (',//ead:archdesc//ead:unitid,'')"/>
        <xsl:for-each select=".//ead:container">
            <xsl:value-of select="concat('||',@type, ' ', text())"/>
        </xsl:for-each>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="$rights"/>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="//ead:archdesc//ead:language"/>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="//ead:archdesc//ead:repository/ead:corpname"/>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:for-each select="//ead:archdesc//ead:controlaccess/ead:subject">
          <xsl:if test="position()>1">
            <xsl:text>||</xsl:text>
          </xsl:if>
          <xsl:value-of select="."/>
        </xsl:for-each>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:choose>
          <xsl:when test="ead:did/ead:origination[@label='creator']/ead:persname">
            <xsl:value-of select="ead:did/ead:origination[@label='creator']/ead:persname"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="ancestor::*/ead:did/ead:origination[@label='creator']/ead:persname"/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:text>&quot;</xsl:text>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" version="1.0"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:ead="urn:isbn:1-931666-22-9">
    <xsl:output method="text"/>
    <xsl:template match="/">
        <xsl:text>Field name</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>EAD ID</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>REF ID</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>Digital Object ID</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>Digital Object Title</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>Publish Digital Object Record</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>File URL of Linked-to digital object</xsl:text>
        <xsl:text>,</xsl:text>
        <xsl:text>File URL of Thumbnail</xsl:text>
        <xsl:text>&#10;</xsl:text>
        
        <xsl:apply-templates select="//ead:c|//ead:c01|//ead:c02|//ead:c03"/>
    </xsl:template>
    
    <xsl:template match="ead:c|ead:c01|ead:c02|ead:c03">

        <xsl:text>&quot;</xsl:text>
        <xsl:text>TBD</xsl:text>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="/ead:ead/ead:eadheader/ead:eadid"/>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="@id"/>

        <xsl:text>&quot;,&quot;</xsl:text>
        <!-- empty -->

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:choose>
            <xsl:when test="ead:did/ead:daogrp/ead:daodesc">
                <xsl:value-of select="ead:did/ead:daogrp[1]/ead:daodesc"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select=".//ead:unittitle"/>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:text>TRUE</xsl:text>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="ead:did/ead:daogrp[1]/ead:daoloc[1]/@xlink:href"/>

        <xsl:text>&quot;,&quot;</xsl:text>
        <xsl:value-of select="ead:did/ead:daogrp[1]/ead:daoloc[2]/@xlink:href"/>

        <xsl:text>&quot;</xsl:text>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>
</xsl:stylesheet>

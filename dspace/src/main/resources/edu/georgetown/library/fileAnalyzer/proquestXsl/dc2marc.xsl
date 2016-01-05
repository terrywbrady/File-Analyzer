<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:marc="http://www.loc.gov/MARC21/slim"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:import href="common.xsl"/>
    <xsl:import href="marc.xsl"/>
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="recdate">20130225090000.10</xsl:param>
    <xsl:param name="university-name">University Name</xsl:param>
    <xsl:param name="university-loc">University Location</xsl:param>

    <xsl:template match="/*">
        <xsl:apply-templates select="." mode="MARC">
        	<xsl:with-param name="pRecdate" select="$recdate"/>
        	<xsl:with-param name="pCreated" select="//dcvalue[@element='date'][@qualifier='created']/text()"/>
        	<xsl:with-param name="pCreator">
        	    <xsl:apply-templates select="//dcvalue[@element='creator']" mode="lfm"/>
        	</xsl:with-param>
        	<xsl:with-param name="pCreatorSuff">
        	    <xsl:apply-templates select="//dcvalue[@element='creator']" mode="suff"/>
        	</xsl:with-param>
        	<xsl:with-param name="pTitle" select="//dcvalue[@element='title'][@qualifier='none']/text()"/>
        	<xsl:with-param name="pnAltTitle" select="//dcvalue[@element='title'][@qualifier='alternative']"/>
        	<xsl:with-param name="pPages" select="//dcvalue[@element='format'][@qualifier='extent']/text()"/>
        	<xsl:with-param name="pDegree" select="//dcvalue[@element='description'][@qualifier='none']/text()"/>
        	<xsl:with-param name="pCollege" select="//dcvalue[@element='source'][@qualifier='none'][1]/text()"/>
       		<xsl:with-param name="pDept" select="//dcvalue[@element='source'][@qualifier='none'][2]/text()"/>
        	<xsl:with-param name="pnAdvisor" select="//dcvalue[@element='contributor'][@qualifier='advisor']"/>
        	<xsl:with-param name="pnAbstract" select="//dcvalue[@element='description'][@qualifier='abstract']"/>
        	<xsl:with-param name="pnCatStr" select="//dcvalue[@element='subject'][@qualifier='lcsh']"/>
        	<xsl:with-param name="pnCat" select="//dcvalue[@element='subject'][@qualifier='other']"/>
        	<xsl:with-param name="pKeyword">
        	  <xsl:for-each select="//dcvalue[@element='subject'][@qualifier='none']">
        	    <xsl:value-of select="."/>
        	    <xsl:if test="position()!=last()">
        	      <xsl:text>, </xsl:text>
        	    </xsl:if>
        	  </xsl:for-each>
        	</xsl:with-param>
        	<xsl:with-param name="pUrl" select="//dcvalue[@element='identifier'][@qualifier='uri']/text()"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="dcvalue" mode="Tag500">
       	<xsl:apply-templates select="." mode="fmls"/>	
    </xsl:template>

    <xsl:template match="dcvalue" mode="Tag520">
       	<xsl:value-of select="."/>	
    </xsl:template>

    <xsl:template match="dcvalue" mode="Tag700">
       	<xsl:value-of select="."/>	
    </xsl:template>

    <xsl:template match="dcvalue" mode="lfm">
    	<xsl:variable name="surname" select="substring-before(text(), ',')"/>
    	<xsl:variable name="fms" select="substring-after(text(), ',')"/>
    	<xsl:variable name="s" select="substring-after($fms, ',')"/>
    	<xsl:variable name="fm" select="substring-before($fms, ',')"/>
    	
    	<xsl:choose>
    	  <xsl:when test="$s=''"><xsl:value-of select="text()"/></xsl:when>
    	  <xsl:otherwise><xsl:value-of select="concat($surname,',',$fm,',')"/></xsl:otherwise>
    	</xsl:choose>
    </xsl:template>

    <xsl:template match="dcvalue" mode="suff">
    	<xsl:variable name="surname" select="substring-before(text(), ',')"/>
    	<xsl:variable name="fms" select="substring-after(text(), ',')"/>
    	<xsl:variable name="s" select="substring-after($fms, ',')"/>
    	<xsl:variable name="fm" select="substring-before($fms, ',')"/>
    	
    	<xsl:choose>
    	  <xsl:when test="$s=''"/>
    	  <xsl:otherwise><xsl:value-of select="$s"/></xsl:otherwise>
    	</xsl:choose>
    </xsl:template>

    <xsl:template match="dcvalue" mode="fmls">
    	<xsl:variable name="surname" select="substring-before(text(), ',')"/>
    	<xsl:variable name="fms" select="substring-after(text(), ',')"/>
    	<xsl:variable name="s" select="substring-after($fms, ',')"/>
    	<xsl:variable name="fm" select="substring-before($fms, ',')"/>
    	
    	<xsl:choose>
    	  <xsl:when test="$surname=''"><xsl:value-of select="text()"/></xsl:when>
    	  <xsl:when test="$fm=''"><xsl:value-of select="concat($fms,' ',$surname)"/></xsl:when>
    	  <xsl:otherwise><xsl:value-of select="concat($fm,' ',$surname, ', ', $s)"/></xsl:otherwise>
    	</xsl:choose>
    	
    </xsl:template>
</xsl:stylesheet>

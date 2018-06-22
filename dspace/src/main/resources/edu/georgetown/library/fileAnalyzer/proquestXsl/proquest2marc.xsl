<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:marc="http://www.loc.gov/MARC21/slim"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:import href="common.xsl"/>
    <xsl:import href="proquest.xsl"/>
    <xsl:import href="marc.xsl"/>
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="recdate">20130225090000.10</xsl:param>
    <xsl:param name="university-name">University Name</xsl:param>
    <xsl:param name="university-loc">University Location</xsl:param>

    <xsl:template match="/*">
        <xsl:apply-templates select="." mode="MARC">
        	<xsl:with-param name="pRecdate" select="$recdate"/>
        	<xsl:with-param name="pCreated" select="//DISS_comp_date/text()"/>
        	<xsl:with-param name="pCreator">
        	    <xsl:apply-templates select="//DISS_author/DISS_name">
        	        <xsl:with-param name="format">lfm</xsl:with-param>
        	    </xsl:apply-templates>
        	</xsl:with-param>
        	<xsl:with-param name="pCreatorSuff">
        	    <xsl:apply-templates select="//DISS_author/DISS_name">
        	        <xsl:with-param name="format">s</xsl:with-param>
        	    </xsl:apply-templates>
        	</xsl:with-param>
        	<xsl:with-param name="pTitle">
				<xsl:call-template name="notags">
                	<xsl:with-param name="str" select="//DISS_title/text()"/>
                </xsl:call-template>
           	</xsl:with-param>
        	<xsl:with-param name="pnAltTitle" select="//DISS_supp_title"/>
        	<xsl:with-param name="pPages" select="concat(//DISS_description/@page_count, ' leaves')"/>
        	<xsl:with-param name="pDegree" select="//DISS_degree/text()"/>
        	<xsl:with-param name="pCollege" select="//DISS_inst_name/text()"/>
       		<xsl:with-param name="pDept" select="//DISS_inst_contact/text()"/>
        	<xsl:with-param name="pnAdvisor" select="//DISS_advisor"/>
        	<xsl:with-param name="pnAbstract" select="//DISS_abstract/DISS_para"/>
        	<xsl:with-param name="pnCatCode" select="//DISS_cat_code"/>
        	<xsl:with-param name="pnCat" select="//DISS_cat_desc"/>
        	<xsl:with-param name="pKeyword" select="//DISS_keyword/text()"/>
        	<xsl:with-param name="pUrl"/>
        </xsl:apply-templates>
    </xsl:template>
    

    <xsl:template match="DISS_advisor" mode="Tag500">
       	<xsl:apply-templates select="DISS_name">
       		<xsl:with-param name="format">fmls</xsl:with-param>
       	</xsl:apply-templates>	
    </xsl:template>

    <xsl:template match="DISS_advisor" mode="Tag520">
       	<xsl:call-template name="notags">
       	  <xsl:with-param name="str" select="text()"/>
       	</xsl:call-template>	
    </xsl:template>

    <xsl:template match="DISS_advisor" mode="Tag700">
       	<xsl:apply-templates select="DISS_name">
       		<xsl:with-param name="format">lfms</xsl:with-param>
       	</xsl:apply-templates>	
    </xsl:template>

</xsl:stylesheet>

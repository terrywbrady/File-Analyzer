<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:import href="common.xsl"/>
  <xsl:import href="proquest.xsl"/>
  <xsl:output method="xml"/>
  <xsl:param name="university-name">University Name</xsl:param>
  <xsl:param name="university-loc">University Location</xsl:param>

  <xsl:template match="/*">
    <xsl:variable name="pCreator" select="//DISS_author/DISS_name"/>
    <xsl:variable name="pCreated" select="//DISS_comp_date"/>
    <xsl:variable name="pTitle" select="//DISS_title"/>
    <xsl:variable name="pDegree" select="//DISS_degree"/>

    <xsl:if test="count($pCreator) = 0" > 
        <xsl:message terminate="Y">An author is required</xsl:message>
    </xsl:if>
    <xsl:if test="count($pCreator) != 1">
        <xsl:message terminate="Y">Only one author is allowed</xsl:message>
    </xsl:if>
    <xsl:if test="count($pCreated) = 0">
        <xsl:message terminate="Y">A completed date is required</xsl:message>
    </xsl:if>
    <xsl:if test="count($pCreated) != 1">
        <xsl:message terminate="Y">Only one comp date is allowed</xsl:message>
    </xsl:if>
    <xsl:if test="string-length($pCreated) != 4">
        <xsl:message terminate="Y">Comp date must be a 4 digit year</xsl:message>
    </xsl:if>
    <xsl:if test="count($pTitle) = 0">
        <xsl:message terminate="Y">A title is required</xsl:message>
    </xsl:if>
    <xsl:if test="count($pTitle) != 1">
        <xsl:message terminate="Y">Only one title is allowed</xsl:message>
    </xsl:if>
    <xsl:if test="count($pDegree) = 0">
        <xsl:message terminate="Y">A degree is required</xsl:message>
    </xsl:if>
    <xsl:if test="count($pDegree) != 1">
        <xsl:message terminate="Y">Only one degree is allowed</xsl:message>
    </xsl:if>


    <dublin_core schema="dc">
      <xsl:call-template name="val">
        <xsl:with-param name="element">creator</xsl:with-param>
        <xsl:with-param name="text">
          <xsl:apply-templates select="$pCreator"/>
        </xsl:with-param>
      </xsl:call-template>

      <xsl:call-template name="val">
        <xsl:with-param name="element">format</xsl:with-param>
        <xsl:with-param name="qualifier">extent</xsl:with-param>
        <xsl:with-param name="text" select="concat(//DISS_description/@page_count, ' leaves')"/>
      </xsl:call-template>

      <xsl:call-template name="val">
        <xsl:with-param name="element">title</xsl:with-param>
        <xsl:with-param name="text">
          <xsl:call-template name="notags">
            <xsl:with-param name="str" select="$pTitle/text()"/>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>

      <xsl:call-template name="val">
        <xsl:with-param name="element">title</xsl:with-param>
        <xsl:with-param name="qualifier">alternative</xsl:with-param>
        <xsl:with-param name="text">
          <xsl:call-template name="notags">
            <xsl:with-param name="str" select="//DISS_supp_title/text()"/>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>

      <xsl:apply-templates select="$pCreated" mode="val">
        <xsl:with-param name="element">date</xsl:with-param>
        <xsl:with-param name="qualifier">created</xsl:with-param>
      </xsl:apply-templates>
 
      <xsl:apply-templates select="$pCreated" mode="val">
        <xsl:with-param name="element">date</xsl:with-param>
        <xsl:with-param name="qualifier">issued</xsl:with-param>
      </xsl:apply-templates>
 
         <!-- reformat to yyyy/mm/dd? -->
      <xsl:apply-templates select="//DISS_accept_date" mode="val">
        <xsl:with-param name="element">date</xsl:with-param>
        <xsl:with-param name="qualifier">submitted</xsl:with-param>
      </xsl:apply-templates>
 
      <xsl:call-template name="val">
        <xsl:with-param name="text" select="$university-name"/>
        <xsl:with-param name="element">publisher</xsl:with-param>
      </xsl:call-template>
 
      <xsl:apply-templates select="//DISS_inst_name" mode="val">
        <xsl:with-param name="element">source</xsl:with-param>
      </xsl:apply-templates>

      <xsl:apply-templates select="//DISS_inst_contact" mode="val">
        <xsl:with-param name="element">source</xsl:with-param>
      </xsl:apply-templates>

      <xsl:for-each select="//DISS_advisor/DISS_name">
          <xsl:apply-templates select="." mode="val">
            <xsl:with-param name="element">contributor</xsl:with-param>
            <xsl:with-param name="qualifier">advisor</xsl:with-param>
            <xsl:with-param name="text"><xsl:apply-templates select="."/></xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>

      <xsl:for-each select="//DISS_cat_code">
        <xsl:variable name="code" select="text()"/>
        <xsl:variable name="ecatcode" select="document('lcsh.xml')//lcsh[@code=$code]"/>
        <xsl:for-each select="$ecatcode">
            <xsl:variable name="catcode" select="val"/>
            <xsl:variable name="nc" select="count($catcode)"/>
 
            <xsl:if test="$nc &gt; 0">
                <xsl:call-template name="val">
                    <xsl:with-param name="element">subject</xsl:with-param>
                    <xsl:with-param name="qualifier">lcsh</xsl:with-param>
                    <xsl:with-param name="text">
                        <xsl:choose>
                            <xsl:when test="$nc &gt; 2">
                                <xsl:value-of select="concat($catcode[1],$lcsh-sep,$catcode[2],$lcsh-sep, $catcode[3])"/>
                            </xsl:when>
                            <xsl:when test="$nc &gt; 1">
                                <xsl:value-of select="concat($catcode[1],$lcsh-sep,$catcode[2])"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$catcode[1]"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                </xsl:call-template>  
            </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:for-each select="//DISS_cat_desc">
        <xsl:apply-templates select="." mode="val">
          <xsl:with-param name="element">subject</xsl:with-param>
          <xsl:with-param name="qualifier">other</xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>
      
      <xsl:if test="//DISS_keyword">
        <xsl:call-template name="keyword">
          <xsl:with-param name="str" select="//DISS_keyword/text()"/>
        </xsl:call-template>
      </xsl:if>
      
      <xsl:apply-templates select="//DISS_language" mode="val">
        <xsl:with-param name="element">language</xsl:with-param>
      </xsl:apply-templates> 

      <xsl:for-each select="//DISS_abstract/DISS_para">
        <xsl:apply-templates select="." mode="val">
          <xsl:with-param name="element">description</xsl:with-param>
          <xsl:with-param name="qualifier">abstract</xsl:with-param>
          <xsl:with-param name="text">
            <xsl:call-template name="notags">
              <xsl:with-param name="str" select="text()"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:for-each>

      <xsl:apply-templates select="$pDegree" mode="val">
        <xsl:with-param name="element">description</xsl:with-param>
      </xsl:apply-templates>

      <xsl:apply-templates select="//DISS_binary/@type" mode="val">
        <xsl:with-param name="element">format</xsl:with-param>
      </xsl:apply-templates>
    </dublin_core>
  </xsl:template>

  <xsl:template name="keyword">
    <xsl:param name="str"/>
    <xsl:choose>
      <xsl:when test="contains($str,',')">
        <xsl:call-template name="val">
          <xsl:with-param name="element">subject</xsl:with-param>
          <xsl:with-param name="text" select="substring-before($str, ',')"/>
        </xsl:call-template>
        <xsl:call-template name="keyword">
          <xsl:with-param name="str" select="substring-after($str, ',')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="val">
          <xsl:with-param name="element">subject</xsl:with-param>
          <xsl:with-param name="text" select="$str"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*|@*" mode="val">
    <xsl:param name="element">element</xsl:param>
    <xsl:param name="qualifier">none</xsl:param>
    <xsl:param name="text" select="."/>
    <xsl:call-template name="val">
      <xsl:with-param name="text" select="$text"/>
      <xsl:with-param name="element" select="$element"/>
      <xsl:with-param name="qualifier" select="$qualifier"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="val">
    <xsl:param name="text" select="."/>
    <xsl:param name="element">element</xsl:param>
    <xsl:param name="qualifier">none</xsl:param>
    <xsl:if test="$text!=''">
    <dcvalue>
      <xsl:attribute name="element">
        <xsl:value-of select="$element"/>
      </xsl:attribute>
      <xsl:attribute name="qualifier">
        <xsl:value-of select="$qualifier"/>
      </xsl:attribute>
      <xsl:value-of select="$text"/>
    </dcvalue>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>

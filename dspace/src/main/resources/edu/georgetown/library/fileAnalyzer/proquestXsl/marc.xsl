<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0" xmlns:marc="http://www.loc.gov/MARC21/slim" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:template match="/*" mode="MARC">
        <xsl:param name="pRecdate" />
        <xsl:param name="pCreated" />
        <xsl:param name="pCreator" />
        <xsl:param name="pCreatorSuff" />
        <xsl:param name="pTitle" />
        <xsl:param name="pnAltTitle" />
        <xsl:param name="pPages" />
        <xsl:param name="pDegree" />
        <xsl:param name="pCollege" />
        <xsl:param name="pDept" />
        <xsl:param name="pnAdvisor" />
        <xsl:param name="pnAbstract" />
        <xsl:param name="pnCatCode" />
        <xsl:param name="pnCatStr" select="/bogus"/>
        <xsl:param name="pnCat" />
        <xsl:param name="pKeyword" />
        <xsl:param name="pUrl" />

        <xsl:if test="string-length($pCreator) = 0">
            <xsl:message terminate="Y">An author is required</xsl:message>
        </xsl:if>
        <xsl:if test="string-length($pCreated) = 0">
            <xsl:message terminate="Y">A completed date is required</xsl:message>
        </xsl:if>
        <xsl:if test="string-length($pCreated) != 4">
            <xsl:message terminate="Y">Comp date must be a 4 digit year</xsl:message>
        </xsl:if>
        <xsl:if test="string-length($pTitle) = 0">
            <xsl:message terminate="Y">A title is required</xsl:message>
        </xsl:if>
        <xsl:if test="string-length($pDegree) = 0">
            <xsl:message terminate="Y">A degree is required</xsl:message>
        </xsl:if>

        <marc:record>
            <xsl:attribute name="xsi:schemaLocation">http://www.loc.gov/MARC21/slim
                http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd</xsl:attribute>
            <marc:leader>     ntm         Ic     </marc:leader>
            <marc:controlfield tag="001">NEW</marc:controlfield>
            <marc:controlfield tag="005">
                <xsl:value-of select="$pRecdate" />
            </marc:controlfield>
 
            <marc:controlfield tag="006">m     o  d</marc:controlfield>
            <marc:controlfield tag="007">cr |||||||||||</marc:controlfield>
            <marc:controlfield tag="008">
                <xsl:text>||||||s</xsl:text>
                <xsl:value-of select="$pCreated" />
                <xsl:text>    dcu     o     000 0 eng</xsl:text>
            </marc:controlfield>
            <marc:datafield tag="040" ind1=" " ind2=" ">
                <marc:subfield code="a">DGU</marc:subfield>
                <marc:subfield code="e">rda</marc:subfield>
                <marc:subfield code="c">DGU</marc:subfield>
            </marc:datafield>
            <marc:datafield tag="049" ind1=" " ind2=" ">
                <marc:subfield code="a">DGUU</marc:subfield>
            </marc:datafield>
            <marc:datafield tag="100" ind1="1" ind2=" ">
                <marc:subfield code="a">
                    <xsl:value-of select="$pCreator" />
                </marc:subfield>
                <xsl:if test="$pCreatorSuff">
                    <marc:subfield code="c">
                        <xsl:value-of select="$pCreatorSuff" />
                    </marc:subfield>
                </xsl:if>
                <marc:subfield code="e">author</marc:subfield>
                
                <marc:subfield code="4">aut</marc:subfield>
            </marc:datafield>
            <marc:datafield tag="245" ind1="1" ind2="0">
                <marc:subfield code="a">
                    <xsl:value-of select="$pTitle" />
                </marc:subfield>
            </marc:datafield>
            <xsl:for-each select="$pnAltTitle">
                <marc:datafield tag="246" ind1="1" ind2=" ">
                    <marc:subfield code="a">
                        <xsl:value-of select="." />
                    </marc:subfield>
                </marc:datafield>
            </xsl:for-each>
            <marc:datafield tag="264"  ind1=" " ind2="0">
                <marc:subfield code="a">
                    <xsl:value-of select="$university-loc"/>
                </marc:subfield>
                <marc:subfield code="b">
                    <xsl:value-of select="$university-name"/>
                </marc:subfield>
                <marc:subfield code="c">
                    <xsl:value-of select="$pCreated" />
                </marc:subfield>
            </marc:datafield>
            <xsl:if test="$pPages">
                <marc:datafield tag="300" ind1=" " ind2=" ">
                    <marc:subfield code="a">
                        <xsl:value-of select="$pPages" />
                    </marc:subfield>
                </marc:datafield>
            </xsl:if>
            <marc:datafield tag="336" ind1=" " ind2=" ">
                <marc:subfield code="a">text</marc:subfield>
                <marc:subfield code="b">txt</marc:subfield>
                <marc:subfield code="2">rdacontent</marc:subfield>
            </marc:datafield>
            <marc:datafield tag="337" ind1=" " ind2=" ">
                <marc:subfield code="a">unmediated</marc:subfield>
                <marc:subfield code="b">n</marc:subfield>
                <marc:subfield code="2">rdamedia</marc:subfield>
            </marc:datafield>
            <marc:datafield tag="338" ind1=" " ind2=" ">
                <marc:subfield code="a">volume</marc:subfield>
                <marc:subfield code="b">nc</marc:subfield>
                <marc:subfield code="2">rdacarrier</marc:subfield>
            </marc:datafield>
            <marc:datafield tag="502" ind1=" " ind2=" ">
                <marc:subfield code="b">
                    <xsl:value-of select="$pDegree" />
                </marc:subfield>
                <xsl:if test="$pCollege or $pDept">
                    <marc:subfield code="c">
                        <xsl:value-of select="concat($pCollege, ', ', $pDept)" />
                    </marc:subfield>
                </xsl:if>
                <marc:subfield code="d">
                    <xsl:value-of select="$pCreated" />
                </marc:subfield>
            </marc:datafield>
            <xsl:for-each select="$pnAdvisor">
                <marc:datafield tag="500" ind1=" " ind2=" ">
                    <marc:subfield code="a">
                        <xsl:text>Advisor: </xsl:text>
                        <xsl:apply-templates select="." mode="Tag500" />
                    </marc:subfield>
                </marc:datafield>
            </xsl:for-each>
            <xsl:for-each select="$pnAbstract">
                <marc:datafield tag="520" ind1=" " ind2=" ">
                    <marc:subfield code="a">
                        <xsl:apply-templates select="." mode="Tag520" />
                    </marc:subfield>
                </marc:datafield>
            </xsl:for-each>
            <xsl:if test="$pnCatCode">
                <xsl:for-each select="$pnCatCode">
                    <marc:datafield tag="650" ind1=" " ind2="0">
                        <xsl:variable name="code" select="text()"/>
                        <xsl:variable name="ecodetext" select="document('lcsh.xml')//lcsh[@code=$code]"/>
                        <xsl:for-each select="$ecodetext">
                            <xsl:variable name="codetext" select="val"/>
                            <marc:subfield code="a">
                                <xsl:value-of select="$codetext[1]" />
                            </marc:subfield>
                            <xsl:for-each select="$codetext[position() &gt; 1]">
                                <marc:subfield code="x">
                                    <xsl:value-of select="." />
                                </marc:subfield>
                            </xsl:for-each>
                        </xsl:for-each>
                    </marc:datafield>
                </xsl:for-each>
            </xsl:if>
            <xsl:for-each select="$pnCatStr">
              <xsl:variable name="line" select="."/>
              <xsl:variable name="item1" select="substring-before($line,$lcsh-sep)"/>
              <xsl:variable name="item23" select="substring-after($line,$lcsh-sep)"/>
              <xsl:variable name="item2" select="substring-before($item23,$lcsh-sep)"/>
              <xsl:variable name="item3" select="substring-after($item23,$lcsh-sep)"/>
              <marc:datafield tag="650" ind1=" " ind2="0">
                <xsl:choose>
                  <xsl:when test="$item1 = ''">
                    <marc:subfield code="a">
                        <xsl:value-of select="$line" />
                    </marc:subfield>
                  </xsl:when>
                  <xsl:when test="$item2 = ''">
                    <marc:subfield code="a">
                        <xsl:value-of select="$item1" />
                    </marc:subfield>
                    <marc:subfield code="x">
                        <xsl:value-of select="$item23" />
                    </marc:subfield>
                  </xsl:when>
                  <xsl:otherwise>
                    <marc:subfield code="a">
                        <xsl:value-of select="$item1" />
                    </marc:subfield>
                    <marc:subfield code="x">
                        <xsl:value-of select="$item2" />
                    </marc:subfield>
                    <marc:subfield code="x">
                        <xsl:value-of select="$item3" />
                    </marc:subfield>
                  </xsl:otherwise>
                </xsl:choose>
              </marc:datafield>
            </xsl:for-each>
            <xsl:for-each select="$pnCat">
                <marc:datafield tag="650" ind1=" " ind2="4">
                    <marc:subfield code="a">
                        <xsl:value-of select="." />
                    </marc:subfield>
                </marc:datafield>
            </xsl:for-each>
            <marc:datafield tag="653" ind1=" " ind2=" ">
                <marc:subfield code="a">
                    <xsl:value-of select="$pKeyword" />
                </marc:subfield>
            </marc:datafield>
            <xsl:for-each select="$pnAdvisor">
                <marc:datafield tag="700" ind1="1" ind2=" ">
                    <marc:subfield code="a">
                        <xsl:apply-templates select="." mode="Tag700" />
                    </marc:subfield>
                    <marc:subfield code="e">advisor</marc:subfield>
                </marc:datafield>
            </xsl:for-each>
            <marc:datafield tag="710" ind1="2" ind2=" ">
                <marc:subfield code="a">
                    <xsl:text><xsl:value-of select="$university-name"/></xsl:text>
                </marc:subfield>
                <marc:subfield code="e">
                    <xsl:text>degree granting institution</xsl:text>
                </marc:subfield>
            </marc:datafield>
            <xsl:if test="$pUrl">
                <marc:datafield tag="856" ind1="4" ind2="0">
                    <marc:subfield code="u">
                        <xsl:value-of select="$pUrl"/>
                    </marc:subfield>
                    <marc:subfield code="z">
                        <xsl:text>CONNECT TO ONLINE RESOURCE.</xsl:text>
                    </marc:subfield>
                </marc:datafield>
            </xsl:if>
        </marc:record>
    </xsl:template>

</xsl:stylesheet>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  xmlns:util="https://github.com/mricaud/xml-util"
  xml:lang="en"
  version="2.0">
  
  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p>XSLT utility library to be used with iso-schematron</xd:p>
    </xd:desc>
  </xd:doc>
  
  <xsl:import href="const.xsl"/>
  
  <xd:doc>
    <xd:desc>check if a element has non-empty text has child</xd:desc>
    <xd:param name="e">Element to check</xd:param>
  </xd:doc>
  <xsl:function name="util:hasText" as="xs:boolean">
    <xsl:param name="e" as="element()"/>
    <xsl:sequence select="exists($e/text()[normalize-space(.)])"/>
  </xsl:function>
  
</xsl:stylesheet>
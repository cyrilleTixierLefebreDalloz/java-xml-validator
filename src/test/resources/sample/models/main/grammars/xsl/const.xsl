<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  xmlns:util="https://github.com/mricaud/xml-util"
  xml:lang="en"
  version="2.0">
  
  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p>Hold usefull constant (no that usefull here but to check nvdl -> sch -> xsl -> xsl inclusion)</xd:p>
    </xd:desc>
  </xd:doc>
  
  <xsl:variable name="util:NCNAME.reg" as="xs:string" select="'[\i-[:]][\c-[:]]*'"/>
  <xsl:variable name="util:QName.reg" as="xs:string" select="'(' || $util:NCNAME.reg || ':)?' || $util:NCNAME.reg"/>  
  
</xsl:stylesheet>
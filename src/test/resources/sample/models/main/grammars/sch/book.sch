<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xml:lang="en"
  queryBinding="xslt2"
  id="book-validation">
  
  
  <ns prefix="xsl" uri="http://www.w3.org/1999/XSL/Transform"/>
  <ns prefix="xs" uri="http://www.w3.org/2001/XMLSchema"/>
  <ns prefix="h" uri="http://www.w3.org/1999/xhtml"/>
  <ns prefix="util" uri="https://github.com/mricaud/xml-util"/>
  
  <xsl:key name="getElementById" match="*[@id]" use="@id"/>
  
  <xsl:include href="../xsl/util.xsl"/>
  
  <!--=============================================================================-->
  <!--PATTERNS-->
  <!--=============================================================================-->
  
  <pattern id="unique-id-constraint">
    <rule context="*[@id]">
      <assert test="count(key('getElementById', current()/@id)) = 1">
        @id="<value-of select="@id"/>" must be unique within the document
      </assert>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="title">
      <report test="contains(., 'error')">
        forbiden word "error" has been found in title content 
      </report>
      <report test="h:span and (*[not(self::h:span)] or util:hasText(.))">
        Don't use inline content when using html span inside title
      </report>
    </rule>
  </pattern>
  
</schema>
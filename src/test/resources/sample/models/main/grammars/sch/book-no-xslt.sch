<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
  queryBinding="xslt2">
  
  <ns prefix="h" uri="http://www.w3.org/1999/xhtml"/>
  
  <!--=============================================================================-->
  <!--PATTERNS-->
  <!--=============================================================================-->
  
  <pattern id="title">
    <rule context="title" id="rule1">
      <report test="h:span and (*[not(self::h:span)] or text()[normalize-space(.)])">
        Don't use inline content when using html span inside title
      </report>
    </rule>
  </pattern>
  
  <pattern>
    <rule context="*">
      <report test="some $t in text() satisfies contains($t, 'error')" role="error">
        "error" has been found 
      </report>
      <report test="some $t in text() satisfies contains($t, 'warning')" role="warning">
        "warning" has been found 
      </report>
    </rule>
  </pattern>
  
</schema>
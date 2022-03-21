<?xml version="1.0" encoding="utf-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xml:lang="en"
  queryBinding="xslt2"
  id="match-text">

  <!--Matching text nodes works in Oxygen but is ignored with warning in Jing-->
  
  <pattern>
    <rule context="text()">
      <report test="contains(., 'error')" role="error">
        forbiden word "error" has been found 
      </report>
    </rule>
  </pattern>
  
</schema>
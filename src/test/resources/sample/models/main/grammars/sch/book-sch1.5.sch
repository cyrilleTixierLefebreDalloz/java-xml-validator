<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
  <sch:pattern name="title-html-or-not">
    <sch:rule context="title">
      <sch:report test="contains(., 'error')">
        forbiden word "error" has been found in title content 
      </sch:report>
    </sch:rule>
  </sch:pattern>
</sch:schema>
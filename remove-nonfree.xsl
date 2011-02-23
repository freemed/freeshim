<?xml version="1.0"?>
<!--
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pom="http://maven.apache.org/POM/4.0.0" version="1.0" >

  <xsl:output method="xml" indent="yes"/> 

  <!-- Remove non-free Topaz stuff -->

  <xsl:template match="pom:module[text()='shim-driver-signature-topaz']"></xsl:template>
  <xsl:template match="pom:dependency[pom:groupId='org.freemedsoftware' and pom:artifactId='shim-driver-signature-topaz']"></xsl:template>
 
  <xsl:template match="comment()">
    <xsl:comment><xsl:value-of select="."/></xsl:comment>
  </xsl:template>
 
  <xsl:template match="*">
    <xsl:copy><xsl:apply-templates/></xsl:copy>
  </xsl:template>
 
</xsl:stylesheet>


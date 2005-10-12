<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Content Stylesheet for "tomcat-site" Documentation -->

<!-- $Id$ -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">


  <!-- Output method -->
  <xsl:output method="xhtml"
              encoding="iso-8859-1"
              doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
              doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
              indent="yes"/>


  <!-- Defined parameters (overrideable) -->
  <xsl:param    name="relative-path" select="'.'"/>

  <!-- Defined variables (non-overrideable) -->
  <xsl:variable name="body-bg"       select="'#ffffff'"/>
  <xsl:variable name="body-fg"       select="'#000000'"/>
  <xsl:variable name="body-link"     select="'#525D76'"/>
  <xsl:variable name="banner-bg"     select="'#525D76'"/>
  <xsl:variable name="banner-fg"     select="'#ffffff'"/>
  <xsl:variable name="sub-banner-bg" select="'#828DA6'"/>
  <xsl:variable name="sub-banner-fg" select="'#ffffff'"/>
  <xsl:variable name="table-th-bg"   select="'#039acc'"/>
  <xsl:variable name="table-td-bg"   select="'#a0ddf0'"/>
  <xsl:variable name="source-color"  select="'#023264'"/>


  <!-- Process an entire document into an HTML page -->
  <xsl:template match="document">
    <xsl:variable name="project"
                select="document('project.xml')/project"/>

    <html>
    <head>
    <xsl:apply-templates select="meta"/>
    <title><xsl:value-of select="$project/title"/> - <xsl:value-of select="properties/title"/></title>
    <xsl:for-each select="properties/author">
      <xsl:variable name="name">
        <xsl:value-of select="."/>
      </xsl:variable>
      <xsl:variable name="email">
        <xsl:value-of select="@email"/>
      </xsl:variable>
      <meta name="author" value="{$name}"/>
      <meta name="email" value="{$email}"/>
    </xsl:for-each>
    <xsl:if test="properties/base">
      <base href="{properties/base/@href}"/>
    </xsl:if>
    </head>

    <body bgcolor="{$body-bg}" text="{$body-fg}" link="{$body-link}"
          alink="{$body-link}" vlink="{$body-link}">

    <table border="0" width="100%" cellspacing="0">

      <xsl:comment>PAGE HEADER</xsl:comment>
      <tr>
        <td>
          <xsl:comment>PROJECT LOGO</xsl:comment>
          <a href="http://tomcat.apache.org/">
            <img src="./images/tomcat.gif" align="left" alt="Tomcat Logo" border="0"/>
          </a>
        </td>
        <td>
          <font face="arial,helvetica,sanserif">
            <h1><xsl:value-of select="$project/title"/></h1>
          </font>
        </td>
        <td>
          <xsl:comment>APACHE LOGO</xsl:comment>
          <a href="http://www.apache.org/">
            <img src="http://www.apache.org/images/asf-logo.gif"
                 align="right" alt="Apache Logo" border="0"/>
          </a>
        </td>
      </tr>
    </table>

    <table border="0" width="100%" cellspacing="4">

      <xsl:comment>HEADER SEPARATOR</xsl:comment>
      <tr>
        <td colspan="2">
          <hr noshade="" size="1"/>
        </td>
      </tr>

      <tr>

        <xsl:comment>LEFT SIDE NAVIGATION</xsl:comment>
        <td width="20%" valign="top" nowrap="true">
          <xsl:apply-templates select="$project/body/menu"/>
        </td>

        <xsl:comment>RIGHT SIDE MAIN BODY</xsl:comment>
        <td width="80%" valign="top" align="left">
          <xsl:apply-templates select="body/section"/>
        </td>

      </tr>

      <xsl:comment>FOOTER SEPARATOR</xsl:comment>
      <tr>
        <td colspan="2">
          <hr noshade="" size="1"/>
        </td>
      </tr>

      <xsl:comment>PAGE FOOTER</xsl:comment>
      <tr><td colspan="2">
        <div align="center"><font color="{$body-link}" size="-1"><em>
        Copyright &#169; 1999-2005, The Apache Software Foundation
        </em></font></div>
      </td></tr>

    </table>
    </body>
    </html>

  </xsl:template>


  <!-- Process a menu for the navigation bar -->
  <xsl:template match="menu">
    <p><strong><xsl:value-of select="@name"/></strong></p>
    <ul>
      <xsl:apply-templates select="item"/>
    </ul>
  </xsl:template>


  <!-- Process a menu item for the navigation bar -->
  <xsl:template match="item">
    <xsl:variable name="href">
      <xsl:choose>
            <xsl:when test="starts-with(@href, 'http://')">
                <xsl:value-of select="@href"/>
            </xsl:when>
            <xsl:when test="starts-with(@href, '/site')">
                <xsl:text>http://tomcat.apache.org</xsl:text><xsl:value-of select="@href"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$relative-path"/><xsl:value-of select="@href"/>
            </xsl:otherwise>    
      </xsl:choose>
    </xsl:variable>
    <li><a href="{$href}"><xsl:value-of select="@name"/></a></li>
  </xsl:template>


  <!-- Process a documentation section -->
  <xsl:template match="section">
    <xsl:variable name="name">
      <xsl:value-of select="@name"/>
    </xsl:variable>
    <table border="0" cellspacing="0" cellpadding="2" width="100%">
      <!-- Section heading -->
      <tr><td bgcolor="{$banner-bg}">
          <font color="{$banner-fg}" face="arial,helvetica,sanserif">
          <a name="{$name}">
          <strong><xsl:value-of select="@name"/></strong></a></font>
      </td></tr>
      <!-- Section body -->
      <tr><td>
      <p><blockquote>
        <xsl:apply-templates/>
      </blockquote></p>
      </td></tr>
      <tr><td><br/></td></tr>
    </table>
  </xsl:template>


  <!-- Process a documentation subsection -->
  <xsl:template match="subsection">
    <xsl:variable name="name">
      <xsl:value-of select="@name"/>
    </xsl:variable>
    <table border="0" cellspacing="0" cellpadding="2" width="100%">
      <!-- Subsection heading -->
      <tr><td bgcolor="{$sub-banner-bg}">
          <font color="{$sub-banner-fg}" face="arial,helvetica,sanserif">
          <a name="{$name}">
          <strong><xsl:value-of select="@name"/></strong></a></font>
      </td></tr>
      <!-- Subsection body -->
      <tr><td><blockquote>
        <xsl:apply-templates/>
      </blockquote></td></tr>
      <tr><td><br/></td></tr>
    </table>
  </xsl:template>


  <!-- Process a source code example -->
  <xsl:template match="source">
    <div align="left">
      <table cellspacing="4" cellpadding="0" border="0">
        <tr>
          <td bgcolor="{$source-color}" width="1" height="1">
            <img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
          <td bgcolor="{$source-color}" height="1">
            <img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
          <td bgcolor="{$source-color}" width="1" height="1">
            <img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
        </tr>
        <tr>
          <td bgcolor="{$source-color}" width="1">
            <img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
          <td bgcolor="#ffffff" height="1"><pre>
            <xsl:value-of select="."/>
          </pre></td>
          <td bgcolor="{$source-color}" width="1">
            <img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
        </tr>
        <tr>
          <td bgcolor="{$source-color}" width="1" height="1">
            <img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
          <td bgcolor="{$source-color}" height="1">
            <img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
          <td bgcolor="{$source-color}" width="1" height="1">
            <img src="/images/void.gif" width="1" height="1" vspace="0" hspace="0" border="0"/>
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <!-- specially process td tags ala site.vsl -->
  <xsl:template match="table[@class='detail-table']/tr/td">
    <td bgcolor="{$table-td-bg}" valign="top" align="left">
        <xsl:if test="@colspan"><xsl:attribute name="colspan"><xsl:value-of select="@colspan"/></xsl:attribute></xsl:if>
        <xsl:if test="@rowspan"><xsl:attribute name="rowspan"><xsl:value-of select="@rowspan"/></xsl:attribute></xsl:if>
        <font color="#000000" size="-1" face="arial,helvetica,sanserif">
            <xsl:apply-templates/>
        </font>
    </td>
  </xsl:template>
  
  <!-- handle th ala site.vsl -->
  <xsl:template match="table[@class='detail-table']/tr/th">
    <td bgcolor="{$table-th-bg}" valign="top">
        <xsl:if test="@colspan"><xsl:attribute name="colspan"><xsl:value-of select="@colspan"/></xsl:attribute></xsl:if>
        <xsl:if test="@rowspan"><xsl:attribute name="rowspan"><xsl:value-of select="@rowspan"/></xsl:attribute></xsl:if>
        <font color="#000000" size="-1" face="arial,helvetica,sanserif">
            <xsl:apply-templates />
        </font>
    </td>
  </xsl:template>
  
  <!-- Process everything else by just passing it through -->
  <xsl:template match="*|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

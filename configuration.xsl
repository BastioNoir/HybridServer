<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:l="http://www.esei.uvigo.es/dai/hybridserver">
	<xsl:output method="html" indent="yes" encoding="utf-8"/>
	
	<xsl:template match="/">
		<xsl:text disable-output-escaping="yes">&lt;!DOCTYPE HTML&gt;</xsl:text>
		<html>
			<head>
				<title>Hybrid Server Configuration</title>
			</head>
			<body>
				<h1>Connections Data</h1>
				<!-- Consultamos connections-->
				<xsl:apply-templates select="l:configuration/l:connections"/>
				<h1>Main Database Data</h1>
				<!-- Consultamos database-->
				<xsl:apply-templates select="l:configuration/l:database"/>
				<h1>Aditional Servers Data</h1>
				<!-- Consultamos servers-->
				<xsl:apply-templates select="l:configuration/l:servers"/>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="l:connections">
		<div>
			<h2><xsl:value-of select="l:http"/></h2>
			<h2><xsl:value-of select="l:webservice"/></h2>
			<h2><xsl:value-of select="l:numClients"/></h2>
		</div>
	</xsl:template>

	<xsl:template match="l:database">
		<div>
			<h2><xsl:value-of select="l:user"/></h2>
			<h2><xsl:value-of select="l:password"/></h2>
			<h2><xsl:value-of select="l:url"/></h2>
		</div>
	</xsl:template>

	<xsl:template match="l:servers">
		<div>
			<xsl:for-each select="l:server">
				<h2><xsl:value-of select="@name"/></h2>

				<h3><xsl:value-of select="@wsdl"/></h3>
				<h3><xsl:value-of select="@namespace"/></h3>
				<h3><xsl:value-of select="@service"/></h3>
				<h3><xsl:value-of select="@httpAddress"/></h3>
			</xsl:for-each>
		</div>
	</xsl:template>

</xsl:stylesheet>
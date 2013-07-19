<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:edm="http://www.europeana.eu/schemas/edm/" xmlns:enrichment="http://www.europeana.eu/schemas/edm/enrichment/" xmlns:ore="http://www.openarchives.org/ore/terms/" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:wgs84="http://www.w3.org/2003/01/geo/wgs84_pos#" xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:xml="http://www.w3.org/XML/1998/namespace" xmlns:eclap="http://www.eclap.eu/ECLAPSchemaV0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<xmlsplitted>
		<rdf:RDF xsi:schemaLocation="http://www.w3.org/1999/02/22-rdf-syntax-ns#
G:\Bellini\Documents\eclap\EDM\schema\EDM.xsd">
			<xsl:for-each select="//eclap:Content">
				<edm:ProvidedCHO>
					<xsl:attribute name="rdf:about"><xsl:value-of select="@axoid"/></xsl:attribute>
					<xsl:copy-of select="eclap:DublinCoreMetadata/dcterms:*"/>
					<xsl:if test="eclap:Resource/eclap:Duration">
						<dcterms:extent>
							<xsl:value-of select="eclap:Resource/eclap:Duration"/>
						</dcterms:extent>
					</xsl:if>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:FirstPerformance/eclap:Date">
						<dcterms:issued>
							<xsl:value-of select="."/> (first performance)</dcterms:issued>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:Performance/eclap:Date">
						<dcterms:issued>
							<xsl:value-of select="."/>
						</dcterms:issued>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:HistoricalPeriod">
						<dcterms:temporal>
							<xsl:value-of select="."/>
						</dcterms:temporal>
					</xsl:for-each>
					<xsl:for-each select="eclap:Classification/eclap:term[@root='637']">
						<dcterms:temporal>
							<xsl:attribute name="rdf:resource">http://www.eclap.eu/Classification/HistoricalPeriod/<xsl:value-of select="@id"/></xsl:attribute>
						</dcterms:temporal>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:Performance/eclap:Place">
						<dcterms:spatial>
							<xsl:value-of select="."/>
						</dcterms:spatial>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:Performance/eclap:City">
						<dcterms:spatial>
							<xsl:value-of select="."/>
						</dcterms:spatial>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:Performance/eclap:Country">
						<dcterms:spatial>
							<xsl:value-of select="."/>
						</dcterms:spatial>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:PieceRecord">
						<dcterms:references>
							<xsl:value-of select="."/>
						</dcterms:references>
					</xsl:for-each>
					<xsl:copy-of select="eclap:DublinCoreMetadata/dc:*"/>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:RecordingDate">
						<dc:date>
							<xsl:value-of select="."/> (recording)</dc:date>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:PerformingArtsGroup">
						<dc:creator>
							<xsl:value-of select="."/>
						</dc:creator>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:Professional">
						<dc:contributor>
							<xsl:value-of select="."/> <xsl:if test="@role!='Other'"> (<xsl:value-of select="replace(@role,'_',' ')"/>)</xsl:if>
						</dc:contributor>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:Cast">
						<dc:contributor>
							<xsl:value-of select="."/>
						</dc:contributor>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:PersonRecord">
						<dc:contributor>
							<xsl:value-of select="."/>
						</dc:contributor>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:ProductionRecord">
						<dc:contributor>
							<xsl:value-of select="."/>
						</dc:contributor>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:PlotSummary">
						<dc:description>
							<xsl:if test="../@xml:lang">
								<xsl:attribute name="xml:lang"><xsl:value-of select="../@xml:lang"/></xsl:attribute>
							</xsl:if>
							<xsl:value-of select="."/>
						</dc:description>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:Object">
						<dc:description>
							<xsl:if test="../@xml:lang">
								<xsl:attribute name="xml:lang"><xsl:value-of select="../@xml:lang"/></xsl:attribute>
							</xsl:if>
							<xsl:value-of select="."/>
						</dc:description>
					</xsl:for-each>
					<xsl:for-each select="eclap:Classification/eclap:term[@root='671']">
						<dc:subject>
							<xsl:attribute name="rdf:resource">http://www.eclap.eu/Classification/Subject/<xsl:value-of select="@id"/></xsl:attribute>
						</dc:subject>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:Genre">
						<dc:subject>
							<xsl:if test="../@xml:lang">
								<xsl:attribute name="xml:lang"><xsl:value-of select="../@xml:lang"/></xsl:attribute>
							</xsl:if>
							<xsl:value-of select="."/>
						</dc:subject>
					</xsl:for-each>
					<xsl:for-each select="eclap:Classification/eclap:term[@root='503']">
						<dc:subject>
							<xsl:attribute name="rdf:resource">http://www.eclap.eu/Classification/Genre/<xsl:value-of select="@id"/></xsl:attribute>
						</dc:subject>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:PerformingArtType">
						<dc:type>
							<xsl:if test="../@xml:lang">
								<xsl:attribute name="xml:lang"><xsl:value-of select="../@xml:lang"/></xsl:attribute>
							</xsl:if>
							<xsl:value-of select="."/>
						</dc:type>
					</xsl:for-each>
					<xsl:for-each select="eclap:Classification/eclap:term[@root='664']">
						<dc:type>
							<xsl:attribute name="rdf:resource">http://www.eclap.eu/Classification/PerformingArtsType/<xsl:value-of select="@id"/></xsl:attribute>
						</dc:type>
					</xsl:for-each>
					<xsl:for-each select="eclap:PerformingArtsMetadata/eclap:ArtisticMovementAndActingStyle">
						<dc:type>
							<xsl:if test="../@xml:lang">
								<xsl:attribute name="xml:lang"><xsl:value-of select="../@xml:lang"/></xsl:attribute>
							</xsl:if>
							<xsl:value-of select="."/>
						</dc:type>
					</xsl:for-each>
					<xsl:for-each select="eclap:Classification/eclap:term[@root='676']">
						<dc:type>
							<xsl:attribute name="rdf:resource">http://www.eclap.eu/Classification/ArtisticMovementAndActingStyle/<xsl:value-of select="@id"/></xsl:attribute>
						</dc:type>
					</xsl:for-each>
					<xsl:for-each select="eclap:Classification/eclap:term[@root='638']">
						<dc:type>
							<xsl:attribute name="rdf:resource">http://www.eclap.eu/Classification/ManagementAndOrganization/<xsl:value-of select="@id"/></xsl:attribute>
						</dc:type>
					</xsl:for-each>
					<edm:type>
						<xsl:choose>
							<xsl:when test="eclap:Resource/eclap:Format='video'">VIDEO</xsl:when>
							<xsl:when test="eclap:Resource/eclap:Format='audio'">AUDIO</xsl:when>
							<xsl:when test="eclap:Resource/eclap:Format='image'">IMAGE</xsl:when>
							<xsl:when test="eclap:Resource/eclap:Format='document'">TEXT</xsl:when>
							<xsl:when test="eclap:Resource/eclap:Format='3D'">3D</xsl:when>
						</xsl:choose>
					</edm:type>
				</edm:ProvidedCHO>
				<edm:WebResource>
					<xsl:attribute name="rdf:about">http://www.eclap.eu/europeana/<xsl:value-of select="@axoid"/></xsl:attribute>
					<edm:rights>
						<xsl:value-of select="eclap:IPR/eclap:EuropeanaRightsUrl"/>
					</edm:rights>
				</edm:WebResource>
				<ore:Aggregation>
					<xsl:attribute name="rdf:about"><xsl:value-of select="@axoid"/>:aggregation</xsl:attribute>
					<edm:aggregatedCHO>
						<xsl:attribute name="rdf:resource"><xsl:value-of select="@axoid"/></xsl:attribute>
					</edm:aggregatedCHO>
					<edm:dataProvider>
						<xsl:value-of select="eclap:ProviderName"/>
					</edm:dataProvider>
					<edm:provider>ECLAP, e-library for Performing Arts</edm:provider>
					<edm:rights>
						<xsl:value-of select="eclap:IPR/eclap:EuropeanaRightsUrl"/>
					</edm:rights>
					<edm:isShownAt>
						<xsl:attribute name="rdf:resource">http://www.eclap.eu/europeana/<xsl:value-of select="@axoid"/></xsl:attribute>
					</edm:isShownAt>
					<xsl:if test="eclap:Preview">
						<edm:object>
							<xsl:attribute name="rdf:resource"><xsl:value-of select="eclap:Preview"/></xsl:attribute>
						</edm:object>
					</xsl:if>
				</ore:Aggregation>
				<xsl:for-each select="eclap:Classification/eclap:term">
					<skos:Concept>
						<xsl:attribute name="rdf:about">http://www.eclap.eu/Classification/<xsl:choose>
							<xsl:when test="@root=503">Genre/</xsl:when>
							<xsl:when test="@root=671">Subject/</xsl:when>
							<xsl:when test="@root=637">HistoricalPeriod/</xsl:when>
							<xsl:when test="@root=664">PerformingArtsType/</xsl:when>
							<xsl:when test="@root=676">ArtisticMovementAndActingStyle/</xsl:when>
							<xsl:when test="@root=638">ManagementAndOrganization/</xsl:when>
						</xsl:choose><xsl:value-of select="@id"/></xsl:attribute>
						<xsl:for-each select="eclap:label">
							<skos:prefLabel>
								<xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
								<xsl:value-of select="."/>
							</skos:prefLabel>
						</xsl:for-each>						
					</skos:Concept>
				</xsl:for-each>
				<xsl:if test="eclap:Aggregate">
					<ore:Proxy>
						<xsl:attribute name="rdf:about"><xsl:value-of select="@axoid"/>:collection</xsl:attribute>
						<ore:proxyFor>
							<xsl:attribute name="rdf:resource"><xsl:value-of select="@axoid"/></xsl:attribute>
						</ore:proxyFor>
						<ore:proxyIn>
							<xsl:attribute name="rdf:resource"><xsl:value-of select="@axoid"/>:aggregation</xsl:attribute>
						</ore:proxyIn>
						<xsl:for-each select="eclap:Aggregate/eclap:ContentRef">
							<dcterms:hasPart>
								<xsl:attribute name="rdf:resource"><xsl:value-of select="."/>:collection_item</xsl:attribute>								
							</dcterms:hasPart>
						</xsl:for-each>
					</ore:Proxy>
					<xsl:for-each select="eclap:Aggregate/eclap:ContentRef">
						<ore:Proxy>
							<xsl:attribute name="rdf:about"><xsl:value-of select="."/>:collection_item</xsl:attribute>
							<ore:proxyFor>
								<xsl:attribute name="rdf:resource"><xsl:value-of select="."/></xsl:attribute>
							</ore:proxyFor>
							<ore:proxyIn>
								<xsl:attribute name="rdf:resource"><xsl:value-of select="."/>:aggregation</xsl:attribute>
							</ore:proxyIn>
							<xsl:if test="preceding-sibling::*[1]">
								<edm:isNextInSequence><xsl:value-of select="preceding-sibling::*[1]"/>:collection_item</edm:isNextInSequence>
							</xsl:if>
						</ore:Proxy>
					</xsl:for-each>					
				</xsl:if>
			</xsl:for-each>
		</rdf:RDF>
		</xmlsplitted>
	</xsl:template>
</xsl:stylesheet>

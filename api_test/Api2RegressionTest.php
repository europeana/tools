<?php

include 'lib/Api2.php';
include 'lib/ErrorTypes.php';
include 'lib/ScreenScrapper.php';
class Api2RegressionTest extends PHPUnit_Framework_TestCase {

  private $types = array('IMAGE', 'TEXT', 'VIDEO', 'SOUND');
  private $keyParam;
  private $guidPattern;
  private $linkPattern;
  private $idPattern;
  private $lastUrl;
  private $screenScrapper;
  private static $errors = array();

  function setUp() {
    $this->keyParam = "wskey=" . API_KEY;
    $this->recordPattern = "@^" . CANONICAL_SERVER . '/portal/record/[^/]+/[^/]+\.html$@';
    $this->guidPattern = "@^http://(www\.)?" . DOMAIN . '/portal/record/[^/]+/[^/]+\.html\?utm_source=api&utm_medium=api&utm_campaign=' . API_KEY . '$@';
    $this->linkPattern = "@^http://(www\.)?" . DOMAIN . API_PATH . '/v2/record/[^/]+/[^/]+\.json\?' . $this->keyParam . '$@';
    $this->idPattern = "@^/[^/]+/[^/]+$@";
    $this->screenScrapper = new ScreenScrapper(PORTAL_SERVER);
    ErrorTypes::init();
  }

  function testPaths() {
    $api = new Api2();
    $this->assertEquals('/v2/search.json', $api->getSearchPath());
    $this->assertEquals('/v2/opensearch.rss', $api->getOpenSearchPath());
    $this->assertEquals('/v2/record/[ID].json', $api->getObjectPath());
    $this->assertEquals('/v2/record/111/222.json', $api->getObjectPath('/111/222'));
  }

  function tearDown() {
    // echo 'Last url was ', $this->lastUrl, LN;
  }

  /**
   * This function runs when every test finished
   */
  public static function tearDownAfterClass() {

    $title = "Europeana Data Reporter";
    $subtitle = "issue " . date("l, F j, Y");
    $titlePadding = floor((80 - strlen($title)) / 2);
    $subtitlePadding = floor((80 - strlen($subtitle)) / 2);

    $handle = fopen("europeana-data-reporter-" . date("Y-m-d") . ".txt", "w");

    fwrite($handle, str_repeat("=", 80) . LN);
    fwrite($handle, str_repeat(" ", $titlePadding) . $title . str_repeat(" ", $titlePadding) . LN);
    fwrite($handle, str_repeat(" ", $subtitlePadding) . $subtitle . str_repeat(" ", $subtitlePadding) . LN);
    fwrite($handle, str_repeat("=", 80) . LN);
    fwrite($handle, LN . LN . LN);

    foreach (Api2RegressionTest::$errors as $bean => $msgKey) {
      if ($bean == 'Statistics') {
        fwrite($handle, LN . "Statistical information" . LN);
      } else {
        fwrite($handle, LN . "Problems with " . $bean . LN);
      }
      foreach ($msgKey as $key => $msgo) {
        fwrite($handle, LN . $key . LN);
        foreach ($msgo as $msg => $c) {
          fwrite($handle, TB . $msg . LN);
        }
      }
    }
    fclose($handle);
  }
  
  /// Search related tests

  function testSearch() {
    $api = new Api2();
    $query = "paris";
    $results = $api->search($query);
    $this->lastUrl = $api->getLastUrl();
    $hits = $this->screenScrapper->getHitsOnPortal($query);
    $this->_checkSearchResult($results, $hits);
  }

  function testSearchWithCallback() {
    $api = new Api2();
    $query = "paris";
    $results = $api->search($query, 1, 12, "print");
    $hits = $this->screenScrapper->getHitsOnPortal($query);
    $this->lastUrl = $api->getLastUrl();
    $this->assertNotNull($results, "Result should not be null");
    $this->assertTrue(is_string($results), "Results should be string");
    $this->assertTrue(preg_match("/^print\(.*?\)/s", $results) == 1, "Callback should be the part of result");
    $results = trim(preg_replace("/^print\((.*?)\);$/s", "$1", $results));
    $resultsObject = json_decode($results);
    $this->assertTrue(is_object($resultsObject), "Results should be object after json_decode");
    $this->_checkSearchResult($resultsObject, $hits);
  }

  function testGeoSearch() {
    $api = new Api2();
    $query = "pl_wgs84_pos_lat:[1 TO 90]";
    $hits = $this->screenScrapper->getHitsOnPortal($query);
    $start = 1;
    for ($j = 0; $j < 20; $j++) {
      $results = $api->search($query, $start);
      $this->lastUrl = $api->getLastUrl();
      $this->_checkSearchResult($results, $hits);
      $start += 12;
    }
  }

  function testAgentSearch() {
    $api = new Api2();
    $fields = array(
      'edm_agent', 'ag_dc_date', 'ag_dc_identifier', 'ag_edm_begin', 'ag_edm_end',
      'ag_edm_hasMet', 'ag_edm_isRelatedTo', 'ag_edm_wasPresentAt', 'ag_foaf_name',
      'ag_owl_sameAs', 'ag_rdagr2_biographicalInformation', 'ag_rdagr2_dateOfBirth',
      'ag_rdagr2_dateOfDeath', 'ag_rdagr2_dateOfEstablishment', 'ag_rdagr2_dateOfTermination',
      'ag_rdagr2_gender', 'ag_rdagr2_professionOrOccupation', 'ag_skos_note',
      'ag_owl_sameAs', 'ag_skos_prefLabel', 'ag_skos_altLabel', 'ag_skos_hiddenLabel'
    );
    foreach ($fields as $field) {
      $query = $field . ':*';
      $results = $api->search($query, 1, 0);

      $this->assertNotNull($results->totalResults);

      $hits = $this->screenScrapper->getHitsOnPortal($query);
      $this->assertEquals($hits, $results->totalResults, sprintf("The hits in API and Portal should be equal for query %s", $query));

      if ($results->totalResults > 0) {
        $this->assertGreaterThan(0, $results->totalResults, sprintf("%s field should result more than 0 hits", $field));
      }
      $this->error(ErrorTypes::$STAT_AG, sprintf("%s: %d", $field, $results->totalResults));
    }
  }

  function testConceptSearch() {
    $api = new Api2();
    $fields = array(
      'skos_concept', 'cc_skos_prefLabel', 'cc_skos_altLabel', 'cc_skos_hiddenLabel',
      'cc_skos_broader', 'cc_skos_broaderLabel', 'cc_skos_narrower', 'cc_skos_related',
      'cc_skos_broadMatch', 'cc_skos_narrowMatch', 'cc_skos_relatedMatch',
      'cc_skos_exactMatch', 'cc_skos_closeMatch', 'cc_skos_note', 'cc_skos_notation',
      'cc_skos_inScheme'
    );
    foreach ($fields as $field) {
      $query = $field . ':*';
      $results = $api->search($query, 1, 0);

      $this->assertNotNull($results->totalResults);

      $hits = $this->screenScrapper->getHitsOnPortal($query);
      $this->assertEquals($hits, $results->totalResults, sprintf("The hits in API and Portal should be equal for query %s", $query));

      if ($results->totalResults > 0) {
        $this->assertGreaterThan(0, $results->totalResults, sprintf("%s field should result more than 0 hits", $field));
      }
      $this->error(ErrorTypes::$STAT_CC, sprintf("%s: %d", $field, $results->totalResults));
    }
  }

  function testPlaceSearch() {
    $api = new Api2();
    $fields = array(
      'edm_place', 'pl_wgs84_pos_lat', 'pl_wgs84_pos_long', 'pl_wgs84_pos_alt',
      'pl_wgs84_pos_lat_long', 'pl_skos_note', 'pl_dcterms_hasPart',
      'pl_dcterms_isPartOf', 'pl_dcterms_isPartOf_label', 'pl_owl_sameAs',
      'pl_skos_prefLabel', 'pl_skos_altLabel', 'pl_skos_hiddenLabel'
    );
    foreach ($fields as $field) {
      $query = $field . ':*';
      $results = $api->search($query, 1, 0);
      $this->assertNotNull($results->totalResults);

      $hits = $this->screenScrapper->getHitsOnPortal($query);
      $this->assertEquals($hits, $results->totalResults, sprintf("The hits in API and Portal should be equal for query %s", $query));

      if ($results->totalResults > 0) {
        $this->assertGreaterThan(0, $results->totalResults, sprintf("%s field should result more than 0 hits", $field));
      }
      $this->error(ErrorTypes::$STAT_PL, sprintf("%s: %d", $field, $results->totalResults));
    }
  }

  function testAggregationSearch() {
    $api = new Api2();
    $fields = array(
      'provider_aggregation_ore_aggregation', 'provider_aggregation_ore_aggregates',
      'provider_aggregation_edm_aggregatedCHO', 'provider_aggregation_edm_dataProvider',
      'provider_aggregation_edm_hasView', 'provider_aggregation_edm_isShownAt',
      'provider_aggregation_edm_isShownBy', 'provider_aggregation_edm_object',
      'provider_aggregation_edm_provider', 'provider_aggregation_dc_rights',
      'provider_aggregation_edm_rights', 'provider_aggregation_edm_unstored',
      'edm_UGC', 'edm_previewNoDistribute'
    );
    foreach ($fields as $field) {
      $query = $field . ':*';
      $results = $api->search($query, 1, 0);
      $this->assertNotNull($results->totalResults);

      $hits = $this->screenScrapper->getHitsOnPortal($query);
      $this->assertEquals($hits, $results->totalResults, sprintf("The hits in API and Portal should be equal for query %s", $query));

      if ($results->totalResults > 0) {
        $this->assertGreaterThan(0, $results->totalResults, sprintf("%s field should result more than 0 hits", $field));
      }
      $this->error(ErrorTypes::$STAT_AGR, sprintf("%s: %d", $field, $results->totalResults));
    }
  }

  function testEuropeanaAggregationSearch() {
    $api = new Api2();
    $fields = array(
      'edm_europeana_aggregation', 'europeana_aggregation_dc_creator',
      'europeana_aggregation_edm_country', 'europeana_aggregation_edm_hasView',
      'europeana_aggregation_edm_isShownBy', 'europeana_aggregation_edm_landingPage',
      'europeana_aggregation_edm_language', 'europeana_aggregation_edm_rights',
      'europeana_aggregation_ore_aggregatedCHO', 'europeana_aggregation_ore_aggregates'
    );
    foreach ($fields as $field) {
      $query = $field . ':*';
      $results = $api->search($query, 1, 0);
      $this->assertNotNull($results->totalResults);

      $hits = $this->screenScrapper->getHitsOnPortal($query);
      $this->assertEquals($hits, $results->totalResults, sprintf("The hits in API and Portal should be equal for query %s", $query));

      if ($results->totalResults > 0) {
        $this->assertGreaterThan(0, $results->totalResults, sprintf("%s field should result more than 0 hits", $field));
      }
      $this->error(ErrorTypes::$STAT_EAGR, sprintf("%s: %d", $field, $results->totalResults));
    }
  }

  function testProxySearch() {
    $api = new Api2();
    $fields = array(
      'edm_europeana_proxy', 'proxy_dc_contributor', 'proxy_dc_coverage',
      'proxy_dc_creator', 'proxy_dc_date', 'proxy_dc_description', 'proxy_dc_format',
      'proxy_dc_identifier', 'proxy_dc_language', 'proxy_dc_publisher',
      'proxy_dc_relation', 'proxy_dc_rights', 'proxy_dc_source', 'proxy_dc_subject',
      'proxy_dc_title', 'proxy_dc_type', 'proxy_dcterms_conformsTo', 'proxy_dcterms_created',
      'proxy_dcterms_extent', 'proxy_dcterms_hasFormat', 'proxy_dcterms_hasPart',
      'proxy_dcterms_hasVersion', 'proxy_dcterms_isFormatOf', 'proxy_dcterms_isPartOf',
      'proxy_dcterms_isReferencedBy', 'proxy_dcterms_isReplacedBy',
      'proxy_dcterms_isRequiredBy', 'proxy_dcterms_issued', 'proxy_dcterms_isVersionOf',
      'proxy_dcterms_medium', 'proxy_dcterms_provenance', 'proxy_dcterms_references',
      'proxy_dcterms_replaces', 'proxy_dcterms_requires', 'proxy_dcterms_spatial',
      'proxy_dcterms_tableOfContents', 'proxy_dcterms_temporal', 'proxy_edm_currentLocation',
      'proxy_edm_hasMet', 'proxy_edm_hasType', 'proxy_edm_incorporates',
      'proxy_edm_isDerivativeOf', 'proxy_edm_isNextInSequence', 'proxy_edm_isRelatedTo',
      'proxy_edm_isRepresentationOf', 'proxy_edm_isSimilarTo', 'proxy_edm_isSuccessorOf',
      'proxy_edm_realizes', 'proxy_edm_rights', 'proxy_edm_type', 'proxy_edm_unstored',
      'proxy_edm_userTags', 'proxy_edm_wasPresentAt', 'proxy_edm_year', 'proxy_ore_proxy',
      'proxy_ore_proxyFor', 'proxy_ore_proxyIn', 'proxy_owl_sameAs',
      'proxy_dcterms_alternative', 'proxy_edm_currentLocation_lat',
      'proxy_edm_currentLocation_lon'
    );
    foreach ($fields as $field) {
      $query = $field . ':*';
      $results = $api->search($query, 1, 0);
      $this->assertNotNull($results->totalResults);

      $hits = $this->screenScrapper->getHitsOnPortal($query);
      $this->assertEquals($hits, $results->totalResults, sprintf("The hits in API and Portal should be equal for query %s", $query));

      if ($results->totalResults > 0) {
        $this->assertGreaterThan(0, $results->totalResults, sprintf("%s field should result more than 0 hits", $field));
      }
      $this->error(ErrorTypes::$STAT_PR, sprintf("%s: %d", $field, $results->totalResults));
    }
  }

  function testPhysicalThingSearch() {
    $api = new Api2();
    $fields = array(
      'edm_physicalThing', 'europeana_pt_dc_contributor', 'europeana_pt_dc_coverage',
      'europeana_pt_dc_creator', 'europeana_pt_dc_date', 'europeana_pt_dc_description',
      'europeana_pt_dc_format', 'europeana_pt_dc_identifier', 'europeana_pt_dc_language',
      'europeana_pt_dc_publisher', 'europeana_pt_dc_relation', 'europeana_pt_dc_rights',
      'europeana_pt_dc_source', 'europeana_pt_dc_subject', 'europeana_pt_dc_title',
      'europeana_pt_dc_type', 'europeana_pt_dcterms_conformsTo', 'europeana_pt_dcterms_created',
      'europeana_pt_dcterms_extent', 'europeana_pt_dcterms_hasFormat',
      'europeana_pt_dcterms_hasVersion', 'europeana_pt_dcterms_isPartOf',
      'europeana_pt_dcterms_isReferencedBy', 'europeana_pt_dcterms_isReplacedBy',
      'europeana_pt_dcterms_isRequiredBy', 'europeana_pt_dcterms_isVersionOf',
      'europeana_pt_dcterms_issued', 'europeana_pt_dcterms_medium',
      'europeana_pt_dcterms_provenance', 'europeana_pt_dcterms_references',
      'europeana_pt_dcterms_replaces', 'europeana_pt_dcterms_requires',
      'europeana_pt_dcterms_spatial', 'europeana_pt_dcterms_tableOfContents',
      'europeana_pt_dcterms_temporal', 'europeana_pt_edm_hasMet',
      'europeana_pt_edm_incorporates', 'europeana_pt_edm_isDerivativeOf',
      'europeana_pt_edm_isNextInSequence', 'europeana_pt_edm_isRelatedTo',
      'europeana_pt_edm_isRepresentationOf', 'europeana_pt_edm_isSimilarTo',
      'europeana_pt_edm_isSuccessorOf', 'europeana_pt_edm_realizes',
      'europeana_pt_edm_rights', 'europeana_pt_edm_type',
      'europeana_pt_edm_wasPresentAt', 'europeana_pt_owl_sameAs'
    );
    foreach ($fields as $field) {
      $query = $field . ':*';
      $results = $api->search($query, 1, 0);
      $this->assertNotNull($results->totalResults);

      $hits = $this->screenScrapper->getHitsOnPortal($query);
      $this->assertEquals($hits, $results->totalResults, sprintf("The hits in API and Portal should be equal for query %s", $query));

      if ($results->totalResults > 0) {
        $this->assertGreaterThan(0, $results->totalResults, sprintf("%s field should result more than 0 hits", $field));
      }
      $this->error(ErrorTypes::$STAT_PT, sprintf("%s: %d", $field, $results->totalResults));
    }
  }

  function testTimespanSearch() {
    $api = new Api2();
    $fields = array(
      'edm_timespan', 'ts_skos_note', 'ts_dcterms_hasPart', 'ts_dcterms_isPartOf', 
      'ts_dcterms_isPartOf_label', 'ts_edm_begin', 'ts_edm_end', 'ts_owl_sameAs',
      'ts_skos_prefLabel', 'ts_skos_altLabel', 'ts_skos_hiddenLabel'
    );
    foreach ($fields as $field) {
      $query = $field . ':*';
      $results = $api->search($query, 1, 0);
      $this->assertNotNull($results->totalResults);

      $hits = $this->screenScrapper->getHitsOnPortal($query);
      $this->assertEquals($hits, $results->totalResults, sprintf("The hits in API and Portal should be equal for query %s", $query));

      if ($results->totalResults > 0) {
        $this->assertGreaterThan(0, $results->totalResults, sprintf("%s field should result more than 0 hits", $field));
      }
      $this->error(ErrorTypes::$STAT_TS, sprintf("%s: %d", $field, $results->totalResults));
    }
  }

  function testWebresourceSearch() {
    $api = new Api2();
    $fields = array(
      'edm_webResource', 'wr_dc_description', 'wr_dc_format', 'wr_dc_rights',
      'wr_dc_source', 'wr_dcterms_conformsTo', 'wr_dcterms_created', 'wr_dcterms_extent',
      'wr_dcterms_hasPart', 'wr_dcterms_isFormatOf', 'wr_dcterms_issued',
      'wr_edm_isNextInSequence', 'wr_edm_rights'
    );
    foreach ($fields as $field) {
      $query = $field . ':*';
      $results = $api->search($query, 1, 0);
      $this->assertNotNull($results->totalResults);

      $hits = $this->screenScrapper->getHitsOnPortal($query);
      $this->assertEquals($hits, $results->totalResults, sprintf("The hits in API and Portal should be equal for query %s", $query));

      if ($results->totalResults > 0) {
        $this->assertGreaterThan(0, $results->totalResults, sprintf("%s field should result more than 0 hits", $field));
      }
      $this->error(ErrorTypes::$STAT_WR, sprintf("%s: %d", $field, $results->totalResults));
    }
  }

  function xtestDynamicFieldSearch() {
    $api = new Api2();
    $fields = array(
      'ag_rdagr2_gender' => array('en'),
      'ag_skos_altLabel' => array('de', 'en', 'es', 'gl'),
      'ag_skos_note' => array('en', 'es', 'gl'),
      'ag_skos_prefLabel' => array(
        'de', 'def', 'en', 'es', 'fi', 'fr', 'gl', 'it', 'nl', 'no', 'pt', 'ru',
        'sv', 'zh'
      ),
      'cc_skos_altLabel' => array('de', 'en', 'es', 'gl'),
      'cc_skos_note' => array(
        'es', 'gl', 'NO', 'NOR', 'ar', 'bg', 'ca', 'cs', 'da', 'de', 'def', 'el',
        'en', 'eng', 'es', 'et', 'eu', 'fi', 'fr', 'gl', 'hu', 'it', 'lt', 'lv',
        'mt', 'nl', 'no', 'pl', 'pt', 'ro', 'ru', 'sk', 'sl', 'sv', 'tr', 'uk', 'zh'
      ),
      'europeana_aggregation_edm_country' => array(
        'def', 'aa', 'af', 'am', 'an', 'ar', 'ay', 'az', 'be', 'bg', 'bn', 'bo',
        'br', 'bs', 'ca', 'co', 'cs', 'cu', 'cv', 'cy', 'da', 'de', 'def', 'dv',
        'dz', 'ee', 'el', 'en', 'eo', 'es', 'et', 'eu', 'fa', 'fi', 'fo', 'fr',
        'fy', 'ga', 'gd', 'gl', 'gn', 'gu', 'gv', 'he', 'hi', 'hr', 'ht', 'hu',
        'hy', 'ia', 'id', 'ie', 'ig', 'ii', 'io', 'is', 'it', 'ja', 'jv', 'ka',
        'kk', 'kl', 'km', 'kn', 'ko', 'ks', 'ku', 'kw', 'ky', 'la', 'lb', 'lg',
        'li', 'ln', 'lo', 'lt', 'lv', 'mg', 'mi', 'mk', 'ml', 'mn', 'mo', 'mr',
        'ms', 'mt', 'na', 'nb', 'ne', 'nl', 'nn', 'no', 'oc', 'om', 'os', 'pl',
        'ps', 'pt', 'qu', 'rm', 'ro', 'ru', 'rw', 'sa', 'sc', 'se', 'sh', 'sk',
        'sl', 'so', 'sq', 'sr', 'st', 'su', 'sv', 'sw', 'ta', 'te', 'tg', 'th',
        'tl', 'to', 'tr', 'tt', 'ty', 'ug', 'uk', 'ur', 'uz', 'vi', 'vo', 'wa',
        'wo', 'xh', 'yi', 'yo', 'zh', 'zu', 'aa', 'ab', 'ace', 'af', 'ak', 'als',
        'am', 'an', 'ang', 'ar', 'arc', 'arz', 'as', 'ast', 'av', 'ay', 'az',
        'ba', 'bar', 'bcl', 'be', 'bg', 'bh', 'bi', 'bjn', 'bm', 'bn', 'bo', 'bpy',
        'br', 'bs', 'bug', 'bxr', 'ca', 'cai', 'cdo', 'ce', 'ceb', 'ch', 'cho',
        'chr', 'chy', 'ckb', 'co', 'cr', 'crh', 'cs', 'csb', 'cu', 'cv', 'cy',
        'da', 'de', 'diq', 'doi', 'dsb', 'dv', 'dz', 'ee', 'efi', 'el', 'en',
        'eo', 'es', 'et', 'eu', 'ext', 'fa', 'ff', 'fi', 'fil', 'fj', 'fo', 'fr',
        'frp', 'frr', 'fur', 'fy', 'ga', 'gag', 'gan', 'gd', 'gl', 'gn', 'grc',
        'gu', 'gv', 'ha', 'hak', 'haw', 'hbs', 'he', 'hi', 'hif', 'ho', 'hr',
        'hsb', 'ht', 'hu', 'hy', 'hz', 'ia', 'id', 'ie', 'ig', 'ik', 'ilo', 'io',
        'is', 'it', 'iu', 'ja', 'jbo', 'jv', 'ka', 'kaa', 'kab', 'kbd', 'kg',
        'ki', 'kj', 'kk', 'kl', 'km', 'kn', 'ko', 'koi', 'krc', 'ks', 'ksh',
        'ku', 'kv', 'kw', 'ky', 'la', 'lad', 'lb', 'lbe', 'lez', 'lg', 'li',
        'lij', 'lmo', 'ln', 'lo', 'lt', 'ltg', 'lv', 'mdf', 'mg', 'mh', 'mhr',
        'mi', 'min', 'mk', 'ml', 'mn', 'mr', 'mrj', 'ms', 'mt', 'mus', 'mwl',
        'my', 'myv', 'mzn', 'na', 'nah', 'nap', 'nb', 'nds', 'ne', 'new', 'ng',
        'nl', 'nn', 'no', 'non', 'nov', 'nrm', 'nso', 'nv', 'ny', 'oc', 'om',
        'or', 'os', 'osx', 'ota', 'pa', 'pam', 'pap', 'pcd', 'pdc', 'pfl', 'pi',
        'pih', 'pl', 'pms', 'pnb', 'pnt', 'ps', 'pt', 'qu', 'rgn', 'rm', 'rmy',
        'rn', 'ro', 'ru', 'rue', 'rw', 'sa', 'sah', 'sc', 'scn', 'sco', 'sd',
        'se', 'sg', 'sh', 'si', 'sk', 'sl', 'sli', 'sm', 'sma', 'sn', 'so', 'sq',
        'sr', 'srn', 'ss', 'st', 'stq', 'su', 'sv', 'sw', 'szl', 'ta', 'tai',
        'te', 'tet', 'tg', 'th', 'ti', 'tk', 'tl', 'tn', 'to', 'tpi', 'tr', 'ts',
        'tt', 'tum', 'tw', 'ty', 'udm', 'ug', 'uk', 'ur', 'uz', 've', 'vec',
        'vep', 'vi', 'vls', 'vo', 'wa', 'war', 'wo', 'wuu', 'xal', 'xh', 'xmf',
        'yi', 'yo', 'za', 'zea', 'zh', 'zu'
      ),
      'pl_skos_note' => array(
        'es', 'aa', 'af', 'am', 'an', 'ar', 'ay', 'az', 'be', 'bg', 'bn', 'bo',
        'br', 'bs', 'ca', 'ch', 'co', 'cs', 'cu', 'cv', 'cy', 'cz', 'da', 'de',
        'def', 'dv', 'dz', 'ee', 'el', 'en', 'eo', 'es', 'et', 'eu', 'fa', 'fi',
        'fo', 'fr', 'fy', 'ga', 'gd', 'gl', 'gn', 'gu', 'gv', 'he', 'hi', 'hr',
        'ht', 'hu', 'hy', 'ia', 'id', 'ie', 'ig', 'ii', 'io', 'is', 'it', 'ja',
        'jv', 'ka', 'kk', 'kl', 'km', 'kn', 'ko', 'ks', 'ku', 'kw', 'ky', 'la',
        'lb', 'lg', 'li', 'ln', 'lo', 'lt', 'lv', 'mg', 'mi', 'mk', 'ml', 'mn',
        'mo', 'mr', 'ms', 'mt', 'na', 'nb', 'ne', 'nl', 'nn', 'no', 'oc', 'om',
        'os', 'pl', 'ps', 'pt', 'qu', 'rm', 'ro', 'ru', 'rw', 'sa', 'sc', 'se',
        'sg', 'sh', 'sk', 'sk-SK', 'sl', 'so', 'sq', 'sr', 'ss', 'st', 'su',
        'sv', 'sw', 'ta', 'te', 'tg', 'th', 'tl', 'to', 'tr', 'tt', 'ty', 'ug',
        'uk', 'ur', 'uz', 'vi', 'vo', 'wa', 'wo', 'xh', 'yi', 'yo', 'za', 'zh',
        'zu'
      ),
      'provider_aggregation_dc_rights' => array(
        'EN', 'IS', 'IT', 'LT', 'PL', 'SI', 'bul', 'ca', 'cas', 'de', 'el', 'elen',
        'en', 'eng', 'et', 'fr', 'fre', 'ger', 'gre', 'it', 'nl', 'pl', 'ru',
        'rus', 'slv', 'sv', 'ukr', 'zxx'
      ),
      'provider_aggregation_edm_dataProvider' => array('def'),
      'provider_aggregation_edm_provider' => array('def'),
      'provider_aggregation_edm_rights' => array(
        'def', 'EN', 'IT', 'NOR', 'bul', 'de', 'def', 'el', 'en', 'eng', 'es',
        'est', 'fi', 'fr', 'gl', 'hu', 'it', 'ita', 'nl', 'pl', 'pt', 'ru', 'se',
        'sl', 'da', 'de', 'def', 'en', 'eng', 'es', 'fr', 'gl', 'it', 'pl', 'spa',
        'EN', 'ENG', 'HUN', 'IS', 'IT', 'LT', 'NOR', 'Org', 'SI', 'bg', 'bul',
        'ca', 'cas', 'cs', 'da', 'de', 'def', 'el', 'en', 'eng', 'es', 'et', 'fi',
        'fr', 'fre', 'ger', 'gl', 'hu', 'hun', 'is', 'it', 'ita', 'lat', 'lt',
        'nl', 'pl', 'prt', 'pt', 'ro', 'ru', 'rus', 'sl', 'sv', 'zxx', 'EN', 'de',
        'def', 'el', 'en', 'es', 'est', 'fr', 'hu', 'is', 'it', 'lt', 'lv', 'nl',
        'pl', 'pt', 'ro', 'se', 'sk-sk', 'CA', 'DE', 'EL', 'EN', 'ES', 'FA', 'FR',
        'HUN', 'Heb', 'IS', 'IT', 'LT', 'Org', 'PT', 'SI', 'bg', 'bul', 'ca',
        'cas', 'cs', 'cz', 'da', 'danish', 'de', 'def', 'deu', 'dutch', 'el',
        'en', 'eng', 'es', 'est', 'et', 'eu', 'fi', 'fr', 'fra', 'fre', 'ger',
        'german', 'gl', 'gr', 'hu', 'hun', 'is', 'it', 'ita', 'la', 'lt', 'lv',
        'nl', 'nld', 'pl', 'por', 'pt', 'ro', 'ru', 'rus', 'se', 'si', 'sk',
        'sk-sk', 'sl', 'slo', 'slv', 'spa', 'sv', 'zxx', 'EL', 'EN', 'HUN', 'IS',
        'IT', 'LT', 'Org', 'bg', 'bul', 'ca', 'cas', 'de', 'def', 'el', 'en',
        'eng', 'es', 'est', 'fr', 'fre', 'ger', 'german', 'hu', 'it', 'ita', 'nl',
        'pl', 'prt', 'pt', 'ru', 'rus', 'ukr', 'zxx', 'EN', 'ENG', 'HUN', 'IS',
        'IT', 'LT', 'Org', 'SI', 'bg', 'bul', 'ca', 'cas', 'de', 'def', 'el',
        'en', 'eng', 'es', 'est', 'fr', 'fre', 'ger', 'german', 'gr', 'gre', 'hu',
        'hun', 'it', 'ita', 'lv', 'nl', 'pl', 'prt', 'ru', 'rus', 'sl', 'ukr',
        'zxx', 'HUN', 'IT', 'danish', 'de', 'def', 'el', 'en', 'eng', 'es', 'et',
        'fr', 'gr', 'hu', 'is', 'it', 'ita', 'lat', 'nl', 'pl', 'pt', 'ru', 'rus',
        'se', 'sk-sk', 'sl', 'sv', 'ukr', 'IT', 'NOR', 'ca', 'da', 'de', 'def',
        'el', 'en', 'es', 'fr', 'gl', 'hu', 'it', 'nl', 'pl', 'pt', 'ro', 'sl',
        'HUN', 'LT', 'de', 'def', 'en', 'eng', 'es', 'est', 'fr', 'hu', 'it',
        'pl', 'pt', 'ru', 'ENG', 'PL', 'SI', 'de', 'def', 'el', 'elen', 'en',
        'es', 'fr', 'it', 'nl', 'pl', 'prt', 'pt', 'sl', 'HUN', 'IS', 'IT', 'LT',
        'Org', 'SI', 'bul', 'ca', 'cas', 'de', 'def', 'el', 'en', 'eng', 'es',
        'est', 'et', 'fr', 'fre', 'ger', 'german', 'gr', 'gre', 'hun', 'it', 'ita',
        'nl', 'pl', 'pt', 'ro', 'ru', 'rus', 'sl', 'ukr', 'zxx', 'CA', 'DE', 'EN',
        'ENG', 'ES', 'FA', 'FR', 'HUN', 'IT', 'LA', 'NL', 'NOR', 'Org', 'PL',
        'PT', 'RS', 'RU', 'bg', 'bul', 'ca', 'cas', 'cz', 'da', 'de', 'def',
        'deu', 'e', 'el', 'en', 'eng', 'es', 'et', 'fi', 'fr', 'fre', 'ger', 'hu',
        'hun', 'it', 'ita', 'la', 'lat', 'lt', 'lv', 'nl', 'pl', 'pt', 'ro', 'ru',
        'rus', 'sk-SK', 'sl', 'slo', 'sv', 'zxx', 'EL', 'EN', 'ENG', 'HUN', 'IS',
        'IT', 'LT', 'NOR', 'Org', 'SI', 'alt', 'ara', 'arm', 'baq', 'bg', 'bre',
        'bul', 'ca', 'cas', 'cat', 'chi', 'cs', 'cz', 'cze', 'da', 'dan', 'danish',
        'de', 'def', 'deu', 'dit', 'du', 'dut', 'dutch', 'e', 'el', 'elen', 'ell',
        'en', 'eng', 'epo', 'es', 'est', 'et', 'eu', 'fi', 'fin', 'fr', 'fra',
        'fre', 'frm', 'fro', 'ge', 'ger', 'german', 'gl', 'grc', 'gre', 'grm',
        'grp', 'heb', 'hr', 'hu', 'hun', 'is', 'it', 'ita', 'japani', 'la', 'lat',
        'lt', 'lv', 'mul', 'nl', 'nld', 'nor', 'oci', 'ota', 'pl', 'pol', 'por',
        'pr', 'pro', 'prt', 'pt', 'ro', 'ru', 'rus', 'san', 'se', 'sk', 'sk-sk',
        'sl', 'slo', 'spa', 'srp', 'su', 'sv', 'swe', 'tel', 'tur', 'ukr', 'und',
        'ut ', 'vie', 'zxx', 'DE', 'EL', 'EN', 'ENG', 'HUN', 'IS', 'IT', 'LT',
        'NOR', 'Org', 'PL', 'RS', 'RU', 'SI', 'bg', 'bul', 'ca', 'cas', 'da',
        'de', 'def', 'el', 'elen', 'en', 'eng', 'es', 'est', 'et', 'fr', 'fre',
        'ger', 'german', 'gr', 'gre', 'hu', 'hun', 'it', 'ita', 'lv', 'nl', 'pl',
        'prt', 'pt', 'ro', 'ru', 'rus', 'se', 'sl', 'sv', 'ukr', 'zxx', 'HUN',
        'Saksa', 'danish', 'de', 'def', 'dk', 'el', 'en', 'eng', 'englanti',
        'es', 'espanja', 'et', 'fi', 'fr', 'hu', 'it', 'ita', 'italia', 'la',
        'nl', 'norja', 'pl', 'pt', 'ra', 'ransk', 'ranska', 'ro', 'ru', 'ruotsi',
        'saksa', 'se', 'sl', 'slo', 'ss', 'ssv', 'su', 'sv', 'sw', 'tanska', 'uk',
        'unkari', 've'
      ),
      'proxy_dcterms_conformsTo' => array(
        'def', 'EN', 'ENG', 'HUN', 'IS', 'IT', 'LT', 'Org', 'SI', 'bg', 'bul',
        'ca', 'de', 'def', 'el', 'en', 'eng', 'es', 'et', 'fr', 'fre', 'ger',
        'german', 'gr', 'hu', 'hun', 'it', 'ita', 'lat', 'nl', 'pl', 'prt', 'pt',
        'ru', 'rus', 'se', 'sv', 'ukr', 'zxx', 'de', 'def', 'en', 'eng', 'es',
        'hu', 'it', 'ita', 'nl', 'pt'
      ),
      'proxy_dcterms_hasFormat' => array('def', 'ita'),
      'proxy_dcterms_hasVersion' => array('def', 'it'),
      'proxy_dcterms_isFormatOf' => array(
        'def', 'it', 'ca', 'de', 'def', 'en', 'eng', 'es', 'et', 'fr', 'hu', 'it',
        'ita', 'nl', 'pl', 'pt', 'sl', 'def', 'en', 'hu', 'it'
      ),
      'proxy_dcterms_isReplacedBy' => array('def', 'it'),
      'proxy_dcterms_isRequiredBy' => array('def', 'it'),
      'proxy_dcterms_isVersionOf' => array(
        'def', 'it', 'de', 'def', 'en', 'it', 'nl', 'LT', 'NOR', 'bg', 'de',
        'def', 'el', 'en', 'eng', 'et', 'fr', 'fre', 'hu', 'it', 'ita', 'lv',
        'nl', 'pt', 'ru', 'de', 'def', 'en', 'it', 'ita', 'nl'
      ),
      'proxy_dcterms_references' => array('def', 'en', 'it'),
      'proxy_dcterms_replaces' => array('def'),
      'proxy_dcterms_requires' => array(
        'def', 'HUN', 'LT', 'SI', 'bg', 'bul', 'ca', 'de', 'def', 'el', 'en',
        'eng', 'es', 'est', 'et', 'fr', 'hu', 'hun', 'it', 'ita', 'la', 'lat',
        'lv', 'nl', 'pt', 'rus', 'sv', 'zxx'
      ),
      'proxy_dcterms_tableOfContents' => array(
        'def', 'it', 'de', 'def', 'el', 'en', 'et', 'fr', 'hu', 'it', 'la', 'pl',
        'pt', 'ro', 'def', 'en', 'fr', 'nl', 'ru', 'def', 'en', 'es', 'fr', 'nl',
        'ru', 'de', 'en', 'eng', 'fr', 'sv', 'PL', 'de', 'el', 'en', 'fr', 'nl',
        'sv'
      )
    );
    foreach ($fields as $field => $langs) {
      fwrite(STDOUT, $field . LN);
      foreach ($langs as $lang) {
        $query = $field . '.' . $lang . ':*';
        $results = $api->search($query, 1, 0);
        $this->assertNotNull($results->totalResults);

        $hits = $this->screenScrapper->getHitsOnPortal($query);
        $this->assertEquals($hits, $results->totalResults, sprintf("The hits in API and Portal should be equal for query %s", $query));

        if ($results->totalResults > 0) {
          $this->assertGreaterThan(0, $results->totalResults, sprintf("%s field should result more than 0 hits", $field));
        }
        $this->error(ErrorTypes::$STAT_DY, sprintf("%s: %d", $field . '.' . $lang, $results->totalResults));
      }
    }
  }


  /// Object related tests

  function testObjects() {
    $api = new Api2();
    $query = "paris";
    $starts = array(1, 1000, 10000, 100000, 200000, 300000, 400000, 500000, 1000000);

    foreach ($starts as $start) {
      $results = $api->search($query, $start);
      $this->lastUrl = $api->getLastUrl();
      foreach ($results->items as $item) {
        $object = $api->object($item->id);
        $this->lastUrl = $api->getLastUrl();
        $this->_checkObjectResult($object, $item->id);
      }
    }
  }

  function testObjectsWithCallback() {
    $api = new Api2();
    $query = "paris";
    $starts = array(1, 1000, 10000, 100000, 200000, 300000, 400000, 500000, 1000000);

    foreach ($starts as $start) {
      $results = $api->search($query, $start);
      $this->lastUrl = $api->getLastUrl();
      foreach ($results->items as $item) {
        $object = $api->object($item->id, "print");
        $this->lastUrl = $api->getLastUrl();

        $this->assertNotNull($object, "Result should not be null");
        $this->assertTrue(is_string($object), "Results should be string");
        $this->assertTrue(preg_match("/^print\(.*?\)/s", $object) == 1, "Callback should be the part of result");
        $object = trim(preg_replace("/^print\((.*?)\);$/s", "$1", $object));
        $resultsObject = json_decode($object);
        $this->assertTrue(is_object($resultsObject), "Results should be object after json_decode");
        $this->_checkObjectResult($resultsObject, $item->id);
      }
    }
  }

  function testObjectsWithSimilarProfile() {
    $api = new Api2();
    $query = "paris";
    $starts = array(1, 1000, 10000);

    foreach ($starts as $start) {
      $results = $api->search($query, $start);
      $this->lastUrl = $api->getLastUrl();
      foreach ($results->items as $item) {
        $object = $api->object($item->id, "", "similar");
        $this->lastUrl = $api->getLastUrl();
        $this->_checkObjectResult($object, $item->id);
        if (isset($object->similarItems)) {
          $this->assertNotNull($object->similarItems);
          $this->assertTrue(is_array($object->similarItems));
          $this->assertTrue(!empty($object->similarItems));
          foreach ($object->similarItems as $item) {
            $this->_checkSearchItem($item);
          }
        } else {
          // echo "Object without similar items: ", $api->getLastUrl(), LN;
        }
      }
    }
  }

  /// Internals

  private function _checkSearchResult($results, $hits = -1) {
    $this->assertNotNull($results);
    $this->assertNotNull($results->apikey);
    $this->assertEquals(API_KEY, $results->apikey);

    $this->assertNotNull($results->action);
    $this->assertEquals("search.json", $results->action);

    $this->assertNotNull($results->success);
    $this->assertEquals(true, $results->success);

    $this->assertNotNull($results->requestNumber);
    $this->assertNotNull($results->totalResults);
    if ($hits != -1) {
      $this->assertEquals($hits, $results->totalResults, "The hits in API and Portal should be equal");
    }

    $this->assertNotNull($results->itemsCount);
    $this->assertEquals(12, $results->itemsCount);

    $this->assertNotNull($results->items);
    $this->assertTrue(!empty($results->items), "Items should not be empty");
    $this->assertEquals($results->itemsCount, count($results->items), "Nr of items should be the same as itemsCount");

    foreach ($results->items as $item) {
      $this->_checkSearchItem($item);
    }
  }

  private function _checkSearchItem($item) {
    $this->assertNotNull($item->id);
    $this->assertTrue(preg_match($this->idPattern, $item->id) == 1, "ID you be match to id pattern: " . $this->idPattern);
    
    $this->assertNotNull($item->guid);
    $this->assertTrue(preg_match($this->guidPattern, $item->guid) == 1, 
      "Guid you be match to guid pattern: \n" . $this->guidPattern . LN . $item->guid);
    
    $this->assertNotNull($item->link);
    $this->assertTrue(preg_match($this->linkPattern, $item->link) == 1, sprintf("Link %s should be match to link pattern %s", $item->link, $this->linkPattern));
    
    $this->assertNotNull($item->type);
    $this->assertNotNull($item->provider);
    if (isset($item->dataProvider)) {
      $this->assertNotNull($item->dataProvider, sprintf("No data provider for %s (%s)", $item->id, $this->lastUrl));
    } else {
      $this->error(ErrorTypes::$BRIEF_NO_DATA_PROVIDER, sprintf("No data provider for %s (%s)", $item->id, $this->lastUrl));
      // $this->error("BriefBean", "No data provider", sprintf("No data provider for %s (%s)", $item->id, $this->lastUrl));
    }

    if (isset($item->edmPlaceLatitude) || isset($item->edmPlaceLongitude)) {
      $this->assertObjectHasAttribute("edmPlaceLatitude", $item, "edmPlaceLatitude is missing from " . $item->id);
      $this->assertObjectHasAttribute("edmPlaceLongitude", $item, "edmPlaceLongitude is missing from " . $item->id);
      $this->assertNotNull($item->edmPlaceLatitude, "edmPlaceLatitude is null in " . $item->id);
      $this->assertNotNull($item->edmPlaceLongitude, "edmPlaceLongitude is null in " . $item->id);
      $this->assertNotNull(count($item->edmPlaceLatitude), "edmPlaceLatitude is zero-length in " . $item->id);
      $this->assertNotNull(count($item->edmPlaceLongitude), "edmPlaceLongitude is zero-length in " . $item->id);
      $this->assertEquals(count($item->edmPlaceLatitude), count($item->edmPlaceLongitude), "edmPlaceLongitude is missing from " . $item->id);
    }

  }

  private function _checkObjectResult($object, $id) {
    $this->assertNotNull($object);

    $this->assertNotNull($object->apikey);
    $this->assertEquals(API_KEY, $object->apikey);

    $this->assertNotNull($object->action);
    $this->assertEquals("record.json", $object->action);

    $this->assertNotNull($object->success);
    $this->assertEquals(true, $object->success);

    $this->assertNotNull($object->requestNumber);

    $this->assertNotNull($object->object);

    $this->assertNotNull($object->object->type);
    $this->assertTrue(in_array($object->object->type, $this->types));

    $this->assertNotNull($object->object->about);
    $this->assertEquals($id, $object->object->about);

    if ($object->object->europeanaCollectionName[0] != "09102_Ag_EU_MIMO_ESE") {
      if (!isset($object->object->title)) {
        $this->error(ErrorTypes::$FULL_NO_TITLE, sprintf("Title should be set for object %s", $this->clearedLastUrl()));
        // $this->error("FullBean", "No title in object", sprintf("Title should be set for object %s", $this->clearedLastUrl()));
      } else {
        $this->assertObjectHasAttribute('title', $object->object, "Title should be set for object " . $id);
        $this->assertNotNull($object->object->title, "Title should not be empty string for object " . $id);
        $this->assertTrue(is_array($object->object->title));
        foreach ($object->object->title as $title) {
          $this->assertGreaterThan(0, strlen($title), "Title should not be a zero-length string for object " . $id);
        }
      }
    }

    // check type
    $this->assertNotNull($object->object->type);
    $this->assertTrue(in_array($object->object->type, $this->types));

    // check europeanaCompleteness
    $this->assertNotNull($object->object->europeanaCompleteness);
    $this->assertTrue(is_int($object->object->europeanaCompleteness));
    $this->assertTrue($object->object->europeanaCompleteness >= 0 && $object->object->europeanaCompleteness <= 10);

    // check europeanaCollectionName
    $this->assertObjectHasAttribute('europeanaCollectionName', $object->object, "europeanaCollectionName should be set for object " . $id);
    $this->assertNotNull($object->object->europeanaCollectionName, "europeanaCollectionName should not be null for object " . $id);
    $this->assertTrue(is_array($object->object->europeanaCollectionName), "europeanaCollectionName should be an array. " . $id);
    foreach ($object->object->europeanaCollectionName as $name) {
      $this->assertGreaterThan(0, strlen($name), "CollectionName should not be zero-length string for object " . $id);
    }

    // check language
    if (!isset($object->object->language)) {
      $this->error(ErrorTypes::$FULL_NO_LANG, sprintf("Language should be set for object %s", $this->clearedLastUrl()));
      // $this->error("FullBean", "No language in object", sprintf("Language should be set for object %s", $this->clearedLastUrl()));
    } else {
      $this->assertObjectHasAttribute('language', $object->object, "Language should be set for object " . $id);
      $this->assertNotNull($object->object->language, "Language should not be null for object " . $id);
      $this->assertGreaterThan(0, strlen($object->object->language), "Language should not be a zero-length string for object " . $id);
    }

    // entities
    $this->assertNotNull($object->object->proxies);
    $this->assertTrue(is_array($object->object->proxies));

    $this->assertNotNull($object->object->aggregations);
    $this->assertTrue(is_array($object->object->aggregations));

    $this->assertNotNull($object->object->providedCHOs);
    $this->assertTrue(is_array($object->object->providedCHOs));

    $this->assertNotNull($object->object->europeanaAggregation);
    $this->assertTrue(!is_array($object->object->europeanaAggregation));
    $this->assertTrue(is_object($object->object->europeanaAggregation));
    if ($object->object->europeanaAggregation->about != '/aggregation/europeana' . $id) {
      $this->error(ErrorTypes::$FULL_MISFORM_EA_ABOUT, sprintf("Expected: /aggregation/europeana%s actual: %s at %s", $id, $object->object->europeanaAggregation->about, $this->clearedLastUrl()));
      // $this->error("FullBean", "europeanaAggregation@about issue", sprintf("Expected: /aggregation/europeana%s actual: %s at %s", $id, $object->object->europeanaAggregation->about, $this->clearedLastUrl()));
    } else {
      $this->assertEquals('/aggregation/europeana' . $id, $object->object->europeanaAggregation->about, "Different aggregation at " . $this->lastUrl);
    }

    if (isset($object->object->europeanaAggregation->aggregatedCHO)) {
      $this->assertNotNull(isset($object->object->europeanaAggregation->aggregatedCHO), 
        'Aggregated CHO should not be null: ' . $id);
      if ($object->object->europeanaAggregation->aggregatedCHO != '/item' . $id) {
        $this->error(ErrorTypes::$FULL_NO_EACHO, sprintf("Expected: /item/%s actual: %s at %s", $id, $object->object->europeanaAggregation->aggregatedCHO, $this->clearedLastUrl()));
        // $this->error("FullBean", "europeanaAggregation/aggregatedCHO issue", sprintf("Expected: /item/%s actual: %s at %s", $id, $object->object->europeanaAggregation->aggregatedCHO, $this->clearedLastUrl()));
      } else {
        $this->assertEquals('/item' . $id, $object->object->europeanaAggregation->aggregatedCHO, 'Aggregates CHO should match /item/ID: ' . $id);
      }
    } else {
      $this->error(ErrorTypes::$FULL_MISFORM_EA_CHO, sprintf("%s at %s", $id, $this->clearedLastUrl()));
      // $this->error("FullBean", "No europeanaAggregation/aggregatedCHO", sprintf("%s at %s", $id, $this->clearedLastUrl()));
    }

    $this->assertNotNull($object->object->europeanaAggregation->edmLandingPage);
    $this->assertTrue(preg_match($this->recordPattern, $object->object->europeanaAggregation->edmLandingPage) == 1,
        "edmLandingPage you be match to record pattern. ($id)");
    
    if (isset($object->object->places)) {
      $this->assertNotNull($object->object->places);
    }

    if (isset($object->object->agents)) {
      $this->assertNotNull($object->object->agents);
    }

    if (isset($object->object->timespans)) {
      $this->assertNotNull($object->object->timespans);
    }
  }

  private function error($errorType, $msg) {
    $bean = $errorType->getBean();
    $key = $errorType->getKey();
    if (!isset(Api2RegressionTest::$errors[$bean])) {
      Api2RegressionTest::$errors[$bean] = array();
    }
    if (!isset(Api2RegressionTest::$errors[$bean][$key])) {
      Api2RegressionTest::$errors[$bean][$key] = array();
    }
    if (!isset(Api2RegressionTest::$errors[$bean][$key][$msg])) {
      Api2RegressionTest::$errors[$bean][$key][$msg] = 1;
    }
  }

  private function clearedLastUrl() {
    return preg_replace('/\?.*$/', '', $this->lastUrl);
  }
}

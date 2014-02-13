<?php

include 'lib/Api2.php';
include 'lib/ErrorTypes.php';
include 'lib/ScreenScrapper.php';
class Api2RegressionTest extends PHPUnit_Framework_TestCase {

  private $types = array('IMAGE', 'TEXT', 'VIDEO', 'SOUND', '3D');
  private $keyParam;
  private $guidPattern;
  private $linkPattern;
  private $idPattern;
  private $lastUrl;
  private $screenScrapper;
  private $countries = array(
      'AT', 'BE', 'CH', 'CY', 'DE', 'ES', 'EU', 'FI', 'FR', 'GR', 'IT', 'LV', 'LT',
      'LU', 'NL', 'NO', 'PL', 'PT', 'RO', 'SE', 'SI', 'SK', 'UK', 'OTHER'
  );
  private $geolevels = array('Regional', 'National', 'European');
  private $roles = array('Data Aggregator');
  private $scopes = array('Thematic', 'Other/None', 'Cross', 'Single');
  private $domains = array(
      'Library', 'Audio Visual', 'Museum/Gallery', 'Archive', 'Publisher',
      'Research and Educational', 'CrossDomain', 'Gallery', 'Other/None'
  );
  private $datasetStatus = array(
      'Ingestion complete', 'Ready for Harvesting', 'Disabled and Replaced',
      'Ongoing scheduled updates', 'OAI-PHM testing', 'Ready for Replication',
      'Mapping and Normalization'
  );

  private $creationDatePattern = "/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/";
  private $publicationDatePattern = "/^\d{4}-\d{2}-\d{2}$/";

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

  function xtestPaths() {
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

  function xtestSearch() {
    $api = new Api2();
    $query = "paris";
    $results = $api->search($query);
    $this->lastUrl = $api->getLastUrl();
    $hits = $this->screenScrapper->getHitsOnPortal($query);
    $this->_checkSearchResult($results, $hits);
  }

  function xtestFacetRequest() {
    $api = new Api2();
    $query = "*:*";
    $params = array(
        'profile' => 'facets',
        'facet' => 'proxy_dc_contributor',
    );
    $results = $api->search($query, 1, 0, "", array(), $params);
    $this->lastUrl = $api->getLastUrl();
    $this->assertObjectHasAttribute("facets", $results);
    $this->assertEquals(1, count($results->facets));
    $this->assertEquals("proxy_dc_contributor", $results->facets[0]->name);
    $this->assertLessThanOrEqual(750, count($results->facets[0]->fields));
  }

  function xtestFacetRequestWithLimitAndOffset() {
    $api = new Api2();
    $query = "*:*";
    $params = array(
      'profile' => 'facets',
      'facet' => 'proxy_dc_contributor',
      'f.proxy_dc_contributor.facet.limit' => 30,
      'f.proxy_dc_contributor.facet.offset' => 0
    );
    $results = $api->search($query, 1, 0, "", array(), $params);
    $this->lastUrl = $api->getLastUrl();
    $this->assertObjectHasAttribute("facets", $results);
    $this->assertEquals(1, count($results->facets));
    $this->assertEquals("proxy_dc_contributor", $results->facets[0]->name);
    $this->assertLessThanOrEqual(30, count($results->facets[0]->fields));

    $params['f.proxy_dc_contributor.facet.offset'] = 30;
    $results = $api->search($query, 1, 0, "", array(), $params);
    $this->lastUrl = $api->getLastUrl();
    $this->assertObjectHasAttribute("facets", $results);
    $this->assertEquals(1, count($results->facets));
    $this->assertEquals("proxy_dc_contributor", $results->facets[0]->name);
    $this->assertLessThanOrEqual(30, count($results->facets[0]->fields));

    $params['f.proxy_dc_contributor.facet.limit'] = 0;
    $results = $api->search($query, 1, 0, "", array(), $params);
    $this->lastUrl = $api->getLastUrl();
    $this->assertObjectNotHasAttribute("facets", $results);
  }

  function xtestSearchWithCallback() {
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

  function xtestGeoSearch() {
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

  function xtestProviders() {
    $api = new Api2();
    $results = $api->getProviders();
    $this->lastUrl = $api->getLastUrl();
    $this->_checkProviderApiHeaders($results);
    foreach ($results->items as $item) {
      $this->_testProvider($item);
    }
  }

  function xtestProvidersWithOffsetAndLimit() {
    $api = new Api2();
    $results = $api->getProviders(50, 50);
    $this->lastUrl = $api->getLastUrl();
    $this->_checkProviderApiHeaders($results);
    foreach ($results->items as $item) {
      $this->_testProvider($item);
    }
  }

  function xtestProvidersWithConuntryCode() {
    $api = new Api2();
    foreach ($this->countries as $code) {
      $results = $api->getProviders(-1, -1, $code);
      $this->lastUrl = $api->getLastUrl();
      $this->_checkProviderApiHeaders($results);
      foreach ($results->items as $item) {
        $this->_testProvider($item);
      }
    }
  }

  function xtestProvider() {
    $api = new Api2();
    $baseResults = $api->getProviders();
    $this->lastUrl = $api->getLastUrl();
    $this->_checkProviderApiHeaders($baseResults);
    foreach ($baseResults->items as $baseItem) {
      $results = $api->getProvider($baseItem->identifier);
      $this->lastUrl = $api->getLastUrl();
      $this->_checkProviderApiHeaders($results);
      $this->_testProvider($results->items[0]);
    }
  }

  function testProviderDatasets() {
    $api = new Api2();
    $baseResults = $api->getProviders();
    $this->lastUrl = $api->getLastUrl();
    $this->_checkProviderApiHeaders($baseResults);
    foreach ($baseResults->items as $baseItem) {
      $results = $api->getProviderDatasets($baseItem->identifier);
      $this->lastUrl = $api->getLastUrl();
      $this->_checkProviderApiHeaders($results);
      if ($results->itemsCount > 0) {
        foreach ($results->items as $item) {
          $this->_testDataset($item);
        }
      }
    }
  }

  private function _testDataset($item) {
    $this->assertObjectHasAttribute('identifier', $item, 'Each dataset should have identifier');
    $this->assertNotNull($item->identifier);
    $this->assertNotEquals('', $item->identifier);

    $this->assertObjectHasAttribute('provIdentifier', $item, 'Each dataset should have provIdentifier');
    $this->assertNotNull($item->provIdentifier);
    $this->assertNotEquals('', $item->provIdentifier);

    $this->assertObjectHasAttribute('name', $item, 'Each dataset should have name');
    $this->assertNotNull($item->name);
    $this->assertNotEquals('', $item->name);

    $this->assertNotNull($item->status);
    $this->assertNotEquals('', $item->status);
    $this->assertTrue(in_array($item->status, $this->datasetStatus), 'Invalid status: ' . $item->status);

    $this->assertObjectHasAttribute('publishedRecords', $item, 'Each dataset should have publishedRecords');
    $this->assertNotNull($item->publishedRecords);
    $this->assertNotEquals('', $item->publishedRecords);
    $this->assertTrue(is_numeric($item->publishedRecords));

    $this->assertObjectHasAttribute('deletedRecords', $item, 'Each dataset should have deletedRecords');
    $this->assertNotNull($item->deletedRecords);
    $this->assertNotEquals('', $item->deletedRecords);
    $this->assertTrue(is_numeric($item->deletedRecords));

    $this->assertObjectHasAttribute('creationDate', $item, 'Each dataset should have creationDate ' . $this->lastUrl);
    $this->assertNotNull($item->creationDate);
    $this->assertNotEquals('', $item->creationDate);
    $this->assertTrue(preg_match($this->creationDatePattern, $item->creationDate) == 1,
        "creationDate should match to pattern: \n" . $item->creationDate . LN . $this->lastUrl);

    if (isset($item->publicationDate)) {
      $this->assertObjectHasAttribute('publicationDate', $item, 'Each dataset should have publicationDate ' . $this->lastUrl);
      $this->assertNotNull($item->publicationDate);
      $this->assertNotEquals('', $item->publicationDate);
      $this->assertTrue(preg_match($this->publicationDatePattern, $item->publicationDate) == 1,
        "publicationDate should match to pattern: \n" . $item->publicationDate . LN . $this->lastUrl);
    } else {
      echo $item->identifier, ' doesn\'t have publicationDate', LN;
    }
  }

  private function _testProvider($item) {
    $this->assertObjectHasAttribute('identifier', $item, 'Each provider should have identifier');
    $this->assertNotNull($item->identifier);
    $this->assertNotEquals('', $item->identifier);

    $this->assertObjectHasAttribute('country', $item, 'Each provider should have country');
    $this->assertNotNull($item->country);
    $this->assertNotEquals('', $item->country);
    $this->assertTrue(in_array($item->country, $this->countries), 'Invalid country code: ' . $item->country);

    $this->assertObjectHasAttribute('name', $item, 'Each provider should have name');
    $this->assertNotNull($item->name);
    $this->assertNotEquals('', $item->name);

    $this->assertObjectHasAttribute('acronym', $item, 'Each provider should have acronym');
    $this->assertNotNull($item->acronym);
    $this->assertNotEquals('', $item->acronym);

    $this->assertObjectHasAttribute('altname', $item, 'Each provider should have altname');
    $this->assertNotNull($item->altname);
    $this->assertNotEquals('', $item->altname);

    if (isset($item->scope)) {
      // $this->assertObjectHasAttribute('scope', $item, 'Each provider should have scope');
      $this->assertNotNull($item->scope);
      $this->assertNotEquals('', $item->scope);
      $this->assertTrue(in_array($item->scope, $this->scopes), 'Invalid scope: ' . $item->scope);
    } else {
      // echo 'no scope for ', $item->name, LN;
    }

    if (isset($item->domain)) {
      $this->assertNotNull($item->domain);
      $this->assertNotEquals('', $item->domain);
      $this->assertTrue(in_array($item->domain, $this->domains), 'Invalid domain: ' . $item->domain);
    } else {
      // echo 'no domain for ', $item->name, LN;
    }

    if (isset($item->geolevel)) {
      $this->assertObjectHasAttribute('geolevel', $item, 'Each provider should have geolevel');
      $this->assertNotNull($item->geolevel);
      $this->assertNotEquals('', $item->geolevel);
      $this->assertTrue(in_array($item->geolevel, $this->geolevels), 'Invalid geolevel: ' . $item->geolevel);
    } else {
      // echo 'no geolevel for ', $item->name, LN;
    }

    $this->assertObjectHasAttribute('role', $item, 'Each provider should have role');
    $this->assertNotNull($item->role);
    $this->assertNotEquals('', $item->role);
    $this->assertTrue(in_array($item->role, $this->roles), 'Invalid role: ' . $item->role);

    $this->assertObjectHasAttribute('website', $item, 'Each provider should have website');
    $this->assertNotNull($item->website);
    $this->assertNotEquals('', $item->website);
  }

  private function _checkProviderApiHeaders($results) {
    $this->assertNotNull($results);
    $this->assertNotNull($results->apikey);
    $this->assertEquals(API_KEY, $results->apikey);

    $this->assertNotNull($results->action);
    // $this->assertEquals("search.json", $results->action);

    $this->assertNotNull($results->success);
    $this->assertEquals(true, $results->success);

    // $this->assertNotNull($results->requestNumber);
    $this->assertNotNull($results->totalResults);

    $this->assertNotNull($results->itemsCount);
    // $this->assertEquals(12, $results->itemsCount, "The hits in API should be 12 for " . $this->lastUrl);

    if ($results->itemsCount > 0) {
      $this->assertObjectHasAttribute('items', $results, 'The providers call should have items attribute ' . $this->lastUrl);
      $this->assertNotNull($results->items);
      $this->assertTrue(!empty($results->items), "Items should not be empty");
      $this->assertEquals($results->itemsCount, count($results->items), "Nr of items should be the same as itemsCount");
    }
  }
  
  /// Object related tests

  
  function xtestObjects() {
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

  function xtestObjectsWithCallback() {
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

  function xtestObjectsWithSimilarProfile() {
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
    $this->assertEquals(12, $results->itemsCount, "The hits in API should be 12 for " . $this->lastUrl);

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
      foreach ($object->object->language as $language) {
        $this->assertGreaterThan(0, strlen($language), "Language should not be a zero-length string for object " . $id);
      }
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

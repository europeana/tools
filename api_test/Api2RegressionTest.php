<?php

include 'lib/Api2.php';
class Api2RegressionTest extends PHPUnit_Framework_TestCase {

  private $types = array('IMAGE', 'TEXT', 'VIDEO', 'SOUND');
  private $keyParam;
  private $guidPattern;
  private $linkPattern;
  private $idPattern;
  private $lastUrl;
  private static $errors = array();

  function setUp() {
    $this->keyParam = "wskey=" . API_KEY;
    $this->recordPattern = "@^" . CANONICAL_SERVER . '/portal/record/[^/]+/[^/]+\.html$@';
    $this->guidPattern = "@^" . SERVER . '/portal/record/[^/]+/[^/]+\.html\?utm_source=api&utm_medium=api&utm_campaign=' . API_KEY . '$@';
    $this->linkPattern = "@^" . API_SERVER . '/v2/record/[^/]+/[^/]+\.json\?' . $this->keyParam . '$@';
    $this->idPattern = "@^/[^/]+/[^/]+$@";
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
    fwrite(STDOUT, LN . "[REPORT]" . LN);
    foreach (Api2RegressionTest::$errors as $key => $msgo) {
      fwrite(STDOUT, $key . LN);
      foreach ($msgo as $msg => $c) {
        fwrite(STDOUT, TB . $msg . LN);
      }
    }
  }
  
  /// Search related tests

  function testSearch() {
    $api = new Api2();
    $query = "paris";
    $results = $api->search($query);
    $this->lastUrl = $api->getLastUrl();
    $this->_checkSearchResult($results);
  }

  function testSearchWithCallback() {
    $api = new Api2();
    $query = "paris";
    $results = $api->search($query, 1, 12, "print");
    $this->lastUrl = $api->getLastUrl();
    $this->assertNotNull($results, "Result should not be null");
    $this->assertTrue(is_string($results), "Results should be string");
    $this->assertTrue(preg_match("/^print\(.*?\)/s", $results) == 1, "Callback should be the part of result");
    $results = trim(preg_replace("/^print\((.*?)\);$/s", "$1", $results));
    $resultsObject = json_decode($results);
    $this->assertTrue(is_object($resultsObject), "Results should be object after json_decode");
    $this->_checkSearchResult($resultsObject);
  }

  function testGeoSearch() {
    $api = new Api2();
    $query = "pl_wgs84_pos_lat:[1 TO 90]";
    $start = 1;
    for ($j = 0; $j < 20; $j++) {
      $results = $api->search($query, $start);
      $this->lastUrl = $api->getLastUrl();
      $this->_checkSearchResult($results);
      $start += 12;
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

  private function _checkSearchResult($results) {
    $this->assertNotNull($results);
    $this->assertNotNull($results->apikey);
    $this->assertEquals(API_KEY, $results->apikey);

    $this->assertNotNull($results->action);
    $this->assertEquals("search.json", $results->action);

    $this->assertNotNull($results->success);
    $this->assertEquals(true, $results->success);

    $this->assertNotNull($results->requestNumber);
    $this->assertNotNull($results->totalResults);

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
    $this->assertTrue(preg_match($this->linkPattern, $item->link) == 1, "Link you be match to link pattern " . $item->link);
    
    $this->assertNotNull($item->type);
    $this->assertNotNull($item->provider);
    if (isset($item->dataProvider)) {
      $this->assertNotNull($item->dataProvider, sprintf("No data provider for %s (%s)", $item->id, $this->lastUrl));
    } else {
      $this->error("No data provider", sprintf("No data provider for %s (%s)", $item->id, $this->lastUrl));
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
        $this->error("No title in object", sprintf("Title should be set for object %s", $this->lastUrl));
      } else {
        $this->assertObjectHasAttribute('title', $object->object, "Title should be set for object " . $id);
        $this->assertNotNull($object->object->title, "Title should not be empty string for object " . $id);
        $this->assertTrue(is_array($object->object->title));
        foreach ($object->object->title as $title) {
          $this->assertGreaterThan(0, strlen($title), "Title should not be a zero-length string for object " . $id);
        }
      }
    }

    $this->assertNotNull($object->object->type);
    $this->assertTrue(in_array($object->object->type, $this->types));

    $this->assertNotNull($object->object->europeanaCompleteness);
    $this->assertTrue(is_int($object->object->europeanaCompleteness));
    $this->assertTrue($object->object->europeanaCompleteness >= 0 && $object->object->europeanaCompleteness <= 10);

    $this->assertNotNull($object->object->europeanaCollectionName);
    $this->assertTrue(is_array($object->object->europeanaCollectionName), "CollectionName should be an array.");
    foreach ($object->object->europeanaCollectionName as $name) {
      $this->assertGreaterThan(0, strlen($name), "CollectionName should not be zero-length string for object " . $id);
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
      $this->error("europeanaAggregation@about issue", sprintf("Expected: /aggregation/europeana%s actual: %s at %s", $id, $object->object->europeanaAggregation->about, $this->lastUrl));
    } else {
      $this->assertEquals('/aggregation/europeana' . $id, $object->object->europeanaAggregation->about, "Different aggregation at " . $this->lastUrl);
    }

    if (isset($object->object->europeanaAggregation->aggregatedCHO)) {
      $this->assertNotNull(isset($object->object->europeanaAggregation->aggregatedCHO), 
        'Aggregated CHO should not be null: ' . $id);
      if ($object->object->europeanaAggregation->aggregatedCHO != '/item' . $id) {
        $this->error("europeanaAggregation/aggregatedCHO issue", sprintf("Expected: /item/%s actual: %s at %s", $id, $object->object->europeanaAggregation->aggregatedCHO, $this->lastUrl));
      } else {
        $this->assertEquals('/item' . $id, $object->object->europeanaAggregation->aggregatedCHO, 'Aggregates CHO should match /item/ID: ' . $id);
      }
    } else {
      $this->error("No europeanaAggregation/aggregatedCHO", sprintf("%s at %s", $id, $this->lastUrl));
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

  private function error($key, $msg) {
    if (!isset(Api2RegressionTest::$errors[$key])) {
      Api2RegressionTest::$errors[$key] = array();
    }
    if (!isset(Api2RegressionTest::$errors[$key][$msg])) {
      Api2RegressionTest::$errors[$key][$msg] = 1;
    }
  }
}

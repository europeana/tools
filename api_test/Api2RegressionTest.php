<?php

include 'lib/Api2.php';
class Api2RegressionTest extends PHPUnit_Framework_TestCase {

  private $types = array('IMAGE', 'TEXT', 'VIDEO', 'SOUND');
  private $keyParam;
  private $guidPattern;
  private $linkPattern;
  private $idPattern;
  private $lastUrl;

  function setUp() {
    $this->keyParam = "wskey=" . API_KEY;
    $this->recordPattern = "@^" . CANONICAL_SERVER . '/portal/record/[^/]+/[^/]+\.html$@';
    $this->guidPattern = "@^" . CANONICAL_SERVER . '/portal/record/[^/]+/[^/]+\.html\?utm_source=api&utm_medium=api&utm_campaign=' . API_KEY . '$@';
    $this->linkPattern = "@^" . API_SERVER . '//v2/record/[^/]+/[^/]+\.json\?' . $this->keyParam . '$@';
    $this->idPattern = "@^/[^/]+/[^/]+$@";
  }

  function testPaths() {
    $api = new Api2();
    $this->assertEquals($api->getSearchPath(), '/v2/search.json');
    $this->assertEquals($api->getOpenSearchPath(), '/v2/opensearch.rss');
    $this->assertEquals($api->getObjectPath(), '/v2/record/[ID].json');
    $this->assertEquals($api->getObjectPath('/111/222'), '/v2/record/111/222.json');
  }

  function tearDown() {
    // echo 'Last url was ', $this->lastUrl, LN;
  }

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

  private function _checkSearchResult($results) {
    $this->assertNotNull($results);
    $this->assertNotNull($results->apikey);
    $this->assertEquals($results->apikey, API_KEY);

    $this->assertNotNull($results->action);
    $this->assertEquals($results->action, "search.json");

    $this->assertNotNull($results->success);
    $this->assertEquals($results->success, true);

    $this->assertNotNull($results->requestNumber);
    $this->assertNotNull($results->totalResults);

    $this->assertNotNull($results->itemsCount);
    $this->assertEquals($results->itemsCount, 12);

    $this->assertNotNull($results->items);
    $this->assertTrue(!empty($results->items), "Items should not be empty");
    $this->assertEquals(count($results->items), $results->itemsCount, "Nr of items should be the same as itemsCount");

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
      // echo sprintf("No data provider for %s (%s)", $item->id, $this->lastUrl), LN;
    }
  }

  private function _checkObjectResult($object, $id) {
    $this->assertNotNull($object);

    $this->assertNotNull($object->apikey);
    $this->assertEquals($object->apikey, API_KEY);

    $this->assertNotNull($object->action);
    $this->assertEquals($object->action, "record.json");

    $this->assertNotNull($object->success);
    $this->assertEquals($object->success, true);

    $this->assertNotNull($object->requestNumber);

    $this->assertNotNull($object->object);

    $this->assertNotNull($object->object->type);
    $this->assertTrue(in_array($object->object->type, $this->types));

    $this->assertNotNull($object->object->about);
    $this->assertEquals($object->object->about, $id);

    if ($object->object->europeanaCollectionName[0] != "09102_Ag_EU_MIMO_ESE") {
      $this->assertNotNull($object->object->title);
      $this->assertTrue(is_array($object->object->title));
      foreach ($object->object->title as $title) {
        $this->assertGreaterThan(0, strlen($title), "Title should not be empty string: " . $title);
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
      $this->assertGreaterThan(0, strlen($name), "CollectionName should not be empty string: " . $name);
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
    $this->assertEquals($object->object->europeanaAggregation->about, '/aggregation/europeana' . $id);

    if (isset($object->object->europeanaAggregation->aggregatedCHO)) {
      $this->assertNotNull(isset($object->object->europeanaAggregation->aggregatedCHO), 
        'Aggregated CHO should not be null: ' . $id);
      $this->assertEquals($object->object->europeanaAggregation->aggregatedCHO, $id,
        'Aggregates CHO should match ID: ' . $id);
    } else {
      // echo sprintf("No europeanaAggregation/aggregatedCHO %s (%s)", $id, $this->lastUrl), LN;
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
}
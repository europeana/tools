<?php

include 'lib/Api1.php';
class Api1RegressionTest extends PHPUnit_Framework_TestCase {

  private $query;
  private $types = array('IMAGE', 'TEXT', 'VIDEO', 'SOUND');
  private $keyParam;
  private $guidPattern;
  private $linkPattern;
  private $idPattern;

  function setUp() {
    $this->idPattern = "@^/[^/]+/[^/]+$@";
    $this->keyParam = "wskey=" . API_KEY;
    $this->guidPattern = "@^" . CANONICAL_SERVER . '/portal/record/[^/]+/[^/]+\.html$@';
    $this->linkPattern = "@^" . CANONICAL_SERVER . '/api/v1/record/[^/]+/[^/]+\.json\?' . $this->keyParam . '$@';
  }

  function testPaths() {
    $api = new Api1();
    $this->assertEquals($api->getSearchPath(), '/opensearch.json');
    $this->assertEquals($api->getOpenSearchPath(), '/opensearch.rss');
    $this->assertEquals($api->getObjectPath(), '/record/[ID].json');
    $this->assertEquals($api->getObjectPath('/111/222'), '/record/111/222.json');
  }

  function testSearch() {
    $api = new Api1();
    $this->query = "paris";
    $results = $api->search($this->query);
    $this->_checkSearchResult($results);
  }

  function testSearchWithCallback() {
    $api = new Api1();
    $this->query = "paris";
    $results = $api->search($this->query, 1, 12, "print");
    $this->assertNotNull($results, "Result should not be null");
    $this->assertTrue(is_string($results), "Results should be string");
    $this->assertTrue(preg_match("/^print\(.*?\)/s", $results) == 1, "Callback should be the part of result");
    $results = trim(preg_replace("/^print\((.*?)\);$/s", "$1", $results));
    $resultsObject = json_decode($results);
    $this->assertTrue(is_object($resultsObject), "Results should be object after json_decode");
    $this->_checkSearchResult($resultsObject);
  }

  private function _checkSearchResult($results) {
    $this->assertNotNull($results);
    $this->assertNotNull($results->apikey);
    $this->assertEquals($results->apikey, API_KEY);

    $this->assertNotNull($results->action);
    $this->assertEquals($results->action, "search.json");

    $this->assertNotNull($results->success);
    $this->assertEquals($results->success, true);

    $this->assertNotNull($results->description);
    $this->assertEquals($results->description, $this->query . " - Europeana Open Search");

    $this->assertNotNull($results->link);
    $this->assertNotNull($results->totalResults);

    $this->assertNotNull($results->startIndex);
    $this->assertEquals($results->startIndex, 1);

    $this->assertNotNull($results->itemsPerPage);
    $this->assertEquals($results->itemsPerPage, 12);

    $this->assertNotNull($results->items);
    $this->assertTrue(!empty($results->items));
    $this->assertEquals(count($results->items), $results->itemsPerPage);

    foreach ($results->items as $item) {
      $this->_checkSearchItem($item);
    }
  }

  private function _checkSearchItem($item) {
    $this->assertNotNull($item->guid);
    $this->assertTrue(preg_match($this->guidPattern, $item->guid) == 1, $this->guidPattern . LN . $item->guid);
    
    $this->assertNotNull($item->link);
    $this->assertTrue(preg_match($this->linkPattern, $item->link) == 1, $this->linkPattern . LN . $item->link);
  }
}
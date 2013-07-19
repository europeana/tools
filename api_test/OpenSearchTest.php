<?php

include 'lib/Api1.php';
include 'lib/Api2.php';
class Api2RegressionTest extends PHPUnit_Framework_TestCase {

  function testOpenSearch1() {
    $api = new Api1();
    $query = "paris";
    $xml = $api->openSearch($query);

    $this->assertNotNull($xml->channel->title);
    $this->assertEquals($xml->channel->title, "Europeana Open Search");

    $this->assertNotNull($xml->channel->link);
    $this->assertEquals($xml->channel->link, "http://europeana.eu");

    $this->assertNotNull($xml->channel->description);
    $this->assertEquals($xml->channel->description, $query . " - Europeana Open Search");

    // TODO: atom:link

    // opensearch elements
    $totalResults = $xml->xpath('channel/opensearch:totalResults');
    $this->assertEquals(count($totalResults), 1);
    $this->assertGreaterThan(1, (int)$totalResults[0]);

    $startIndex = $xml->xpath('channel/opensearch:startIndex');
    $this->assertEquals(count($startIndex), 1);
    $this->assertEquals((int)$startIndex[0], 1);

    $itemsPerPage = $xml->xpath('channel/opensearch:itemsPerPage');
    $this->assertEquals(count($itemsPerPage), 1);
    $this->assertEquals((int)$itemsPerPage[0], 12);
    
    $Query = $xml->xpath('channel/opensearch:Query');
    $this->assertEquals(count($Query), 1);
    $this->assertEquals($Query[0]['role'], 'request');
    $this->assertEquals($Query[0]['searchTerms'], $query);
    $this->assertEquals((int)$Query[0]['startPage'], 1);

    // image elements
    $this->assertNotNull($xml->channel->image);
    $this->assertNotNull($xml->channel->image->title);
    $this->assertEquals($xml->channel->image->title, "Europeana Open Search");

    $this->assertNotNull($xml->channel->image->link);
    $this->assertEquals($xml->channel->image->link, "http://europeana.eu");
    
    $this->assertNotNull($xml->channel->image->url);
    $this->assertEquals($xml->channel->image->url, "http://www.europeana.eu/portal/sp/img/europeana-logo-en.png");

    $this->assertNotNull($xml->channel->item);
    $this->assertEquals(count($xml->channel->item), (int)$itemsPerPage[0]);

    foreach ($xml->channel->item as $item) {
      $this->assertNotNull($item->guid);
      $this->assertNotNull($item->title);
      $this->assertNotNull($item->link);
      // $this->assertEquals($item->guid, $item->link);
      $this->assertNotNull($item->description);
    }
  }

  function testOpenSearch2() {
    $api = new Api2();
    $query = "paris";
    $xml = $api->openSearch($query);

    $this->assertNotNull($xml->channel->title);
    $this->assertEquals($xml->channel->title, "Europeana Open Search");

    $this->assertNotNull($xml->channel->link);
    $this->assertEquals($xml->channel->link, "http://www.europeana.eu");

    $this->assertNotNull($xml->channel->description);
    $this->assertEquals($xml->channel->description, "Europeana Open Search results");

    // TODO: atom:link

    // opensearch elements
    $totalResults = $xml->xpath('channel/opensearch:totalResults');
    $this->assertEquals(count($totalResults), 1);
    $this->assertGreaterThan(1, (int)$totalResults[0]);

    $startIndex = $xml->xpath('channel/opensearch:startIndex');
    $this->assertEquals(count($startIndex), 1);
    $this->assertEquals((int)$startIndex[0], 1);

    $itemsPerPage = $xml->xpath('channel/opensearch:itemsPerPage');
    $this->assertEquals(count($itemsPerPage), 1);
    $this->assertEquals((int)$itemsPerPage[0], 12);
    
    $Query = $xml->xpath('channel/opensearch:Query');
    $this->assertEquals(count($Query), 1);
    $this->assertEquals($Query[0]['role'], 'request');
    $this->assertEquals($Query[0]['searchTerms'], $query);
    $this->assertEquals((int)$Query[0]['startPage'], 1);

    // image elements
    $this->assertNotNull($xml->channel->image);
    $this->assertNotNull($xml->channel->image->title);
    $this->assertEquals($xml->channel->image->title, "Europeana Open Search");

    $this->assertNotNull($xml->channel->image->link);
    $this->assertEquals($xml->channel->image->link, "http://europeana.eu");
    
    $this->assertNotNull($xml->channel->image->url);
    $this->assertEquals($xml->channel->image->url, "http://www.europeana.eu/portal/sp/img/europeana-logo-en.png");

    $this->assertNotNull($xml->channel->item);
    $this->assertEquals(count($xml->channel->item), (int)$itemsPerPage[0]);

    foreach ($xml->channel->item as $item) {
      $this->assertNotNull($item->guid);
      $this->assertNotNull($item->title);
      $this->assertNotNull($item->link);
      $this->assertEquals($item->guid, $item->link);
      $this->assertNotNull($item->description);
    }
  }
}
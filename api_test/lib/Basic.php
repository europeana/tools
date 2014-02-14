<?php

include_once 'configuration.inc';

/**
 * Basic Europeana API class
 *
 * @author peter.kiraly@kb.nl
 */
abstract class Basic {
  
  /**
   * The server URL
   * @var string
   */
  protected $server;

  /**
   * API key
   * @var string
   */
  protected $apiKey;

  /**
   * The search call's path
   * @var string
   */
  protected $searchPath;

  /**
   * The object call's path
   * @var string
   */
  protected $objectPath;

  /**
   * The OpenSearch call's path
   * @var unknown_type
   */
  protected $openSearchPath;

  /**
   * The URL of last API call
   * @var string
   */
  private $lastUrl;

  /**
   * Object contructor
   * 
   * @param string $server
   *   The server URL
   * @param string $apiKey
   *   API key
   */
  public function __construct($server, $apiKey) {
    $this->server = $server;
    $this->apiKey = $apiKey;
  }

  /**
   * Returns search result
   *
   * @param String $query
   *   Search terms
   * @param int $start
   *   Start element count
   * @param int $rows
   *   Number of hits in a result set
   * @param String $callback
   *   Callback function's name
   *
   * @return mixed|string
   *   If the callback is empty, it returns JSON Object, otherwise it returns JSONP string.
   */
  public function search($query, $start = 1, $rows = 12, $callback = "", $qf = array(), $parameters = array()) {
    $params = $this->getSearchParams($query, $start, $rows, $callback, $qf, $parameters);
    $this->lastUrl = $this->server . $this->searchPath . '?' . $params;
    $content = file_get_contents($this->lastUrl);
    if ($callback == "") {
      return json_decode($content);
    }
    return $content;
  }

  /**
   * Returns object result
   * 
   * @param string $id
   *   Europeana Object ID
   * @param string $callback
   *   Callback function's name
   * @param string $profile
   *   Profile name
   * 
   * @return mixed|string
   *   If the callback is empty, it returns JSON Object, otherwise it returns JSONP string.
   */
  public function object($id, $callback = "", $profile = "") {
    $params = $this->getObjectParams($callback, $profile);
    $objectPath = $this->getObjectPath($id);
    $this->lastUrl = $this->server . $objectPath . '?' . $params;
    $content = file_get_contents($this->lastUrl);
    if ($callback == "") {
      return json_decode($content);
    }
    return $content;
  }

  /**
   * Returns OpenSearch result
   * 
   * @param string $searchTerms
   *   Search terms
   * @param int $startIndex
   *   Start element count
   * @param int $count
   *   Number of hits in a result set
   * 
   * @return SimpleXMLElement
   *   The OpenSearch result as SimpleXMLElement object
   */
  public function openSearch($searchTerms, $startIndex = 1, $count = 12) {
    $params = $this->getOpenSearchParams($searchTerms, $startIndex = 1, $count);
    $this->lastUrl = $this->server . $this->openSearchPath . '?' . $params;
    $content = file_get_contents($this->lastUrl);
    // $dom = new DOMDocument();
    // $dom->loadXML($content);
    // $xml = simplexml_load_string($content);
    $xml = new SimpleXMLElement($content);
    $xml->registerXPathNamespace('enrichment', 'http://www.europeana.eu/schemas/ese/enrichment/');
    $xml->registerXPathNamespace('opensearch', 'http://a9.com/-/spec/opensearch/1.1/');
    $xml->registerXPathNamespace('dc', 'http://purl.org/dc/elements/1.1/');
    $xml->registerXPathNamespace('dcterms', 'http://purl.org/dc/terms/');
    $xml->registerXPathNamespace('europeana', 'http://www.europeana.eu');
    $xml->registerXPathNamespace('atom', 'http://www.w3.org/2005/Atom');

    return $xml;
  }

  /**
   * Returns the last API call's URL
   * 
   * @return string
   *   The URL of last API call
   */
  function getLastUrl() {
    return $this->lastUrl;
  }

  function getSearchPath() {
    return $this->searchPath;
  }

  function getOpenSearchPath() {
    return $this->openSearchPath;
  }

  function getObjectPath($id = FALSE) {
    if ($id === FALSE) {
      return $this->objectPath;
    }
    return str_replace('//', '/', str_replace('[ID]', $id, $this->objectPath));
  }

  function getVariablePath($path, $id = FALSE) {
    if ($id === FALSE) {
      return $path;
    }
    return str_replace('//', '/', str_replace('[ID]', $id, $path));
  }

  private function getOpenSearchParams($searchTerms, $startIndex = 1, $count = 12) {
    $params = array(
      'searchTerms' => $searchTerms,
      'startIndex' => $startIndex,
      'count'  => $count,
    );
    return http_build_query($params, '', '&');
  }

  protected function getContent($path, $params = array(), $callback = '') {
    $this->lastUrl = $this->server . $path . '?' . http_build_query($params, '', '&');
    $content = file_get_contents($this->lastUrl);
    if ($callback == '') {
      return json_decode($content);
    }
    return $content;
  }

  /**
   * Returns the URL parameters of search call
   * 
   * @param string $query
   *   Query terms
   * @param int $startPage
   *   First element in hit list
   * @param int $rows
   *   Number of items in result list
   */
  abstract protected function getSearchParams($query, $startPage = 1, $rows = 12);

  /**
   * Returns the URL parameters of object call
   * 
   * @param string $callback
   *   Callback function name
   * @param string $profile
   *   Object profile
   */
  abstract protected function getObjectParams($callback = "", $profile = "");
}
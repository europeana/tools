<?php

include_once 'Basic.php';

/**
 * Europeana API 1.0 calls
 * 
 * @author peter.kiraly@kb.nl
 */
class Api1 extends Basic {

  protected $searchPath = '/opensearch.json';
  protected $objectPath = '/record/[ID].json';
  protected $openSearchPath = '/opensearch.rss';

  function __construct() {
    parent::__construct(API_SERVER, API_KEY);
  }

  /**
   * Provides search parameters
   * 
   * @see Basic::getSearchParams()
   */
  function getSearchParams($query, $startPage = 1, $rows = 12, $callback = "", $qf = array()) {
    $params = array(
      'searchTerms' => $query,
      'startPage' => $startPage,
      'callback' => $callback,
      'wskey' => $this->apiKey,
    );
    return http_build_query($params, '', '&');
  }

  /**
   * Provides object parameters
   * 
   * @see Basic::getSearchParams()
   */
  protected function getObjectParams($callback = "", $profile = "") {
    $params = array(
        'wskey' => $this->apiKey,
    );
    if ($callback != "") {
      $params['callback'] = $callback;
    }
    return http_build_query($params, '', '&');
  }
}
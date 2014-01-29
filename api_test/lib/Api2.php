<?php

include_once 'Basic.php';

/**
 * Europeana API 2.0 calls
 * 
 * @author peter.kiraly@kb.nl
 */
class Api2 extends Basic {

  protected $searchPath = '/v2/search.json';
  protected $objectPath = '/v2/record/[ID].json';
  protected $openSearchPath = '/v2/opensearch.rss';

  /**
   * List of valid search profiles (in API 2.0)
   * @var array
   */
  private $searchProfiles = array('minimal', 'standard', 'facets', 'breadcrumb', 'portal');

  /**
   * List of valid object profiles (in API 2.0)
   * @var array
   */
  private $objectProfiles = array('full', 'similar');

  function __construct() {
    parent::__construct(API_SERVER, API_KEY);
  }

  /**
   * Provides search parameters
   * 
   * @see Basic::getSearchParams()
   */
  function getSearchParams($query, $start = 1, $rows = 12, $callback = "", $qf = array(), $parameters = array()) {
    $params = array(
      'query' => $query,
      'start' => $start,
      'rows'  => $rows,
      'callback' => $callback,
      'wskey' => $this->apiKey,
    );
    if (!empty($qf)) {
      $params['qf'] = $qf;
    }
    if (!empty($parameters)) {
      foreach ($parameters as $key => $value) {
        $params[$key] = $value;
      }
    }
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
    if ($profile != "" && $this->isValidObjectProfile($profile)) {
      $params['profile'] = $profile;
    }
    return http_build_query($params, '', '&');
  }

  private function isValidObjectProfile($profile) {
    return in_array($profile, $this->objectProfiles);
  }

  private function isValidSearchProfile($profile) {
    return in_array($profile, $this->searchProfiles);
  }
}

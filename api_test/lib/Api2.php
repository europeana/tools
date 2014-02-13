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
  protected $providersPath = '/v2/providers.json';
  protected $providerPath = '/v2/provider/[ID].json';
  protected $providerDatasetsPath = '/v2/provider/[ID]/datasets.json';
  protected $datasetPath = '/v2/dataset/[ID].json';

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
    $query = http_build_query($params, '', '&');
    $query = preg_replace('/%5B[0-9]+%5D/simU', '', $query);
    return $query; //http_build_query($params, '', '&');
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

  public function getProviders($offset = -1, $pageSize = -1, $countryCode = null) {
    $params = array(
      'wskey' => $this->apiKey,
    );
    if ($offset != -1) {
      $params['offset'] = $offset;
    }
    if ($pageSize != -1) {
      $params['pageSize'] = $pageSize;
    }
    if ($countryCode != null) {
      $params['countryCode'] = $countryCode;
    }
    // $this->providersPath
    return $this->getContent($this->providersPath, $params);
  }

  public function getProvider($providerId = FALSE) {
    $params = array(
      'wskey' => $this->apiKey,
    );
    return $this->getContent($this->getVariablePath($this->providerPath, $providerId), $params);
  }

  public function getProviderDatasets($providerId = FALSE) {
    $params = array(
      'wskey' => $this->apiKey,
    );
    return $this->getContent($this->getVariablePath($this->providerDatasetsPath, $providerId), $params);
  }

  public function getDataset($datasetId = FALSE) {
    $params = array(
        'wskey' => $this->apiKey,
    );
    return $this->getContent($this->getVariablePath($this->datasetPath, $datasetId), $params);
  }

  private function isValidObjectProfile($profile) {
    return in_array($profile, $this->objectProfiles);
  }

  private function isValidSearchProfile($profile) {
    return in_array($profile, $this->searchProfiles);
  }
}

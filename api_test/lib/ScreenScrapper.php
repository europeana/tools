<?php

class ScreenScrapper {

  private $portal;

  function __construct($portal) {
    $this->portal = $portal;
    $this->search = $portal . '/search.html';
  }

  public function getHitsOnPortal($query, $qf = array()) {
    $url = $this->search . '?query=' . urlencode($query);
    if (!empty($qf)) {
      foreach ($qf as $q) {
        $url .= '&qf=' . urlencode($qf);
      }
    }

    $text = @file_get_contents($url);
    if ($text === FALSE || strlen($text) == 0) {
      echo "Failed to open stream: HTTP request failed!: " . $url . LN;
      return 0;
    }
    preg_match('/<span class="of-bracket last-record">(.*?)<\/span>/', $text, $matches);
    if (!isset($matches) || empty($matches)) {
      if (preg_match('/<h2>No items found<\/h2>/', $text)) {
        return 0;
      } else {
        echo $url, LN;
        return 0;
      }
    }
    if (!isset($matches[1])) {
      echo $url, LN;
      print_r($matches);
      return 0;
    }
    $hits = (int)str_replace(',', '', $matches[1]);
    return $hits;
  }
}
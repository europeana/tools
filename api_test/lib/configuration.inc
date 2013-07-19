<?php
define('LN', "\n");

// define('SERVER', 'http://preview.europeana.eu');
define('SERVER', 'http://europeana.eu');

define('CANONICAL_SERVER', 'http://www.europeana.eu');

define('PORTAL_SERVER', SERVER . '/portal');
define('API_SERVER', SERVER . '/api');

// TODO: Replace xxx-es with a real API key
define('API_KEY', 'xxxxxx');

function check_plain($text) {
  return htmlspecialchars($text, ENT_QUOTES, 'UTF-8');
}
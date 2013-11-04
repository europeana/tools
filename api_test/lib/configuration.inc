<?php
define('LN', "\n");
define('TB', "\t");

// define('SERVER', 'http://test.portal2.eanadev.org');
// define('SERVER', 'http://preview.europeana.eu');
define('DOMAIN', 'europeana.eu');
define('SERVER', 'http://europeana.eu');

define('CANONICAL_SERVER', 'http://www.europeana.eu');

define('PORTAL_PATH', '/portal');
define('PORTAL_SERVER', SERVER . PORTAL_PATH);
define('API_PATH', '/api');
define('API_SERVER', SERVER . API_PATH);

// TODO: Replace xxx-es with a real API key
define('API_KEY', 'xxxxxxxx');

function check_plain($text) {
  return htmlspecialchars($text, ENT_QUOTES, 'UTF-8');
}

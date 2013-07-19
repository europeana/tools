This project is for testing Europeana API both version 1.0 and 2.0

To run the tests you need PHP (http://php.net), PEAR (PHP Extension and Application 
Repository - http://pear.php.net) and PHPUnit (http://phpunit.de).
The installation guides for these tool are here:

* PHP: http://nl3.php.net/manual/en/install.php
* PEAR: http://pear.php.net/manual/en/installation.php
* PHPUnit: http://phpunit.de/manual/3.7/en/installation.html

To run the tests add API key(s) to lib/configuration.inc then type this command:

  phpunit [test name]

Right now we have the following tests:

* Api1RegressionTest
    Testing API 1.0 JSON and JSONP search
* Api2RegressionTest
    Testing API 2.0 JSON and JSONP search, JSON object, JSON object with "similar" profile, JSONP object
* OpenSearchTest
    Testing API 1.0 and 2.0 OpenSearch

To run all test:

  phpunit Api1RegressionTest
  phpunit Api2RegressionTest
  phpunit OpenSearchTest

For coders
On the main directory you can find the test classes. On the "lib" directory you can
find a very lightweight Europeana API library. The Basic class is the parent of
Api1 and Api2 classes. These children classes contains only the differences specific
to the version 1.0 and 2.0 of the API. The lib/configuration.inc contains configurations
such as the tested server's URL, API keys etc.

If you are looking for a more elaborated library for the version 2.0, please check 
Dan's GitHub project:

  https://github.com/dan-nl/europeana-api

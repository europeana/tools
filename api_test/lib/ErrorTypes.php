<?php

include 'ErrorType.php';

class ErrorTypes {

  static $BRIEF_NO_DATA_PROVIDER;
  static $FULL_NO_TITLE;
  static $FULL_NO_LANG;
  static $FULL_MISFORM_EA_ABOUT;
  static $FULL_NO_EACHO;
  static $FULL_MISFORM_EA_CHO;
  static $PROVIDER_NO_ACRONYM;
  static $PROVIDER_NO_ALTNAME;
  static $PROVIDER_NO_SCOPE;
  static $PROVIDER_NO_DOMAIN;
  static $PROVIDER_NO_GEOLEVEL;
  static $STAT_AG;
  static $STAT_CC;
  static $STAT_PL;
  static $STAT_AGR;
  static $STAT_EAGR;
  static $STAT_PR;
  static $STAT_PT;
  static $STAT_TS;
  static $STAT_WR;
  static $STAT_DY;

  public static function init() {
    self::$BRIEF_NO_DATA_PROVIDER = new ErrorType("BriefBean", "No data provider", "No data provider");
    self::$FULL_NO_TITLE = new ErrorType("FullBean", "No title in object", "No title in object");
    self::$FULL_NO_LANG = new ErrorType("FullBean", "No language in object", "No language in object");
    self::$FULL_MISFORM_EA_ABOUT = new ErrorType("FullBean", "europeanaAggregation@about issue", "europeanaAggregation@about issue");
    self::$FULL_NO_EACHO = new ErrorType("FullBean", "No europeanaAggregation/aggregatedCHO", "No europeanaAggregation/aggregatedCHO");
    self::$FULL_MISFORM_EA_CHO = new ErrorType("FullBean", "europeanaAggregation/aggregatedCHO issue", "europeanaAggregation/aggregatedCHO issue");
    self::$STAT_AG = new ErrorType("Statistics", "Agent", "Statistics for Agent fields");
    self::$STAT_CC = new ErrorType("Statistics", "Concept", "Statistics for Concept fields");
    self::$STAT_PL = new ErrorType("Statistics", "Place", "Statistics for Place fields");
    self::$STAT_AGR = new ErrorType("Statistics", "Aggregation", "Statistics for Aggregation fields");
    self::$STAT_EAGR = new ErrorType("Statistics", "EuropeanaAggregation", "Statistics for EuropeanaAggregation fields");
    self::$STAT_PR = new ErrorType("Statistics", "Proxy", "Statistics for Proxy fields");
    self::$STAT_PT = new ErrorType("Statistics", "PhysicalThing", "Statistics for PhysicalThing fields");
    self::$STAT_TS = new ErrorType("Statistics", "Timespan", "Statistics for Timespan fields");
    self::$STAT_WR = new ErrorType("Statistics", "WebResource", "Statistics for WebResource fields");
    self::$STAT_DY = new ErrorType("Statistics", "Dynamic fields", "Statistics for Dynamic fields");
    self::$PROVIDER_NO_ACRONYM = new ErrorType("Provider", "No acronym in object", "No acronym in object");
    self::$PROVIDER_NO_ALTNAME = new ErrorType("Provider", "No altname in object", "No altname in object");
    self::$PROVIDER_NO_SCOPE = new ErrorType("Provider", "No scope in object", "No scope in object");
    self::$PROVIDER_NO_DOMAIN = new ErrorType("Provider", "No domain in object", "No domain in object");
    self::$PROVIDER_NO_GEOLEVEL = new ErrorType("Provider", "No geolevel in object", "No geolevel in object");
  }

}

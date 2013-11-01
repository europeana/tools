<?php

class ErrorType {

  private $bean;
  private $key;
  private $description;

  function __construct($bean, $key, $description) {
    $this->bean = $bean;
    $this->key = $key;
    $this->description = $description;
  }

  public function getBean() {
    return $this->bean;
  }

  public function setBean($bean) {
    $this->bean = $bean;
  }

  public function getKey() {
    return $this->key;
  }

  public function setKey($key) {
    $this->key = $key;
  }

  public function getDescription() {
    return $this->description;
  }

  public function setDescription($description) {
    $this->description = $description;
  }
}

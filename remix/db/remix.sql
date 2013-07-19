-- phpMyAdmin SQL Dump
-- version 3.4.10.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: May 16, 2013 at 11:50 AM
-- Server version: 5.5.31
-- PHP Version: 5.3.10-1ubuntu3.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `remix`
--

-- --------------------------------------------------------

--
-- Table structure for table `comments`
--

CREATE TABLE IF NOT EXISTS `comments` (
  `comment_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cookie_id` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `timestamp` datetime NOT NULL,
  `video_time` float NOT NULL,
  `language` char(2) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'en',
  `email` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `name` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `comment` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  `admin_flag` tinyint(4) NOT NULL DEFAULT '0',
  `hide` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`comment_id`),
  KEY `cookie_id` (`cookie_id`),
  KEY `language` (`language`),
  KEY `email` (`email`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `cookies`
--

CREATE TABLE IF NOT EXISTS `cookies` (
  `cookie_id` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `language` char(2) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'en',
  `create_time` datetime NOT NULL,
  `load_time` datetime DEFAULT NULL,
  PRIMARY KEY (`cookie_id`),
  KEY `language` (`language`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `flag`
--

CREATE TABLE IF NOT EXISTS `flag` (
  `comment_id` bigint(20) NOT NULL,
  `cookie_id` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `timestamp` datetime NOT NULL,
  PRIMARY KEY (`comment_id`,`cookie_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `userCake_Groups`
--

CREATE TABLE IF NOT EXISTS `userCake_Groups` (
  `Group_ID` int(11) NOT NULL AUTO_INCREMENT,
  `Group_Name` varchar(225) NOT NULL,
  PRIMARY KEY (`Group_ID`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1;

--
-- Dumping data for table `Groups`
--

INSERT INTO `Groups` (`Group_ID`, `Group_Name`) VALUES (1, 'Standard User');
--- need to also add an administrator group, decide on it’s id and name
--- and useaccordingly within the application


-- --------------------------------------------------------

--
-- Table structure for table `userCake_Users`
--

CREATE TABLE IF NOT EXISTS `userCake_Users` (
  `User_ID` int(11) NOT NULL AUTO_INCREMENT,
  `Username` varchar(150) NOT NULL,
  `Username_Clean` varchar(150) NOT NULL,
  `Password` varchar(225) NOT NULL,
  `Email` varchar(150) NOT NULL,
  `ActivationToken` varchar(225) NOT NULL,
  `LastActivationRequest` int(11) NOT NULL,
  `LostPasswordRequest` int(1) NOT NULL DEFAULT '0',
  `Active` int(1) NOT NULL,
  `Group_ID` int(11) NOT NULL,
  `SignUpDate` int(11) NOT NULL,
  `LastSignIn` int(11) NOT NULL,
  PRIMARY KEY (`User_ID`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

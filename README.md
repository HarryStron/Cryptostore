### Requirements ###
Java version 1.8

### Setup client ###
For the client to be able to encrypt and decrypt files the Unlimited Strength JCE is required. To set it up:

* go to [this](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) link and download the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8
* go to jdk1.8 path under *"./Contents/home/jre/lib/security/"* (for OSX machines is normally *"/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/lib/security/"*)
* Extract the contents of the downloaded file and replace the existing ones (remember to keep a backup)


### To setup database and add a default admin with Username "Harry", password "P4$$w0rd" and encryption password "password" ###


```
#!mySQL

CREATE DATABASE cs391;

CREATE TABLE `user_credentials` (
  `username` varchar(12) NOT NULL,
  `hash` varchar(64) NOT NULL,
  `salt` varchar(25) NOT NULL,
  `encPass` varchar(64) NOT NULL,
  `encSalt` varchar(25) NOT NULL,
  `isAdmin` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO `user_credentials` (`username`, `hash`, `salt`, `encPass`, `encSalt`, `isAdmin`)
VALUES
	('Harry', '54704ACCDE01E05487AAF1069A71F11D401D3ABC7F6785EAD6C7F9E50F5DD942', '9ipQKcPXFyjdoVKXOQI0yg==', '3AECDF584BB0A03B79D999DA842A679A8361B0C4B04E3CCCC44601D6A89B0E65', '5jm+ZANPea3YqNrINlfnvg==', 1);
```
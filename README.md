# BSc final year project (2015) #
# Cryptostore #
## [1] Requirements ##
**All steps were tested on a fresh installation of Linux Mint v17.3**

### [1.1] Java ###
Java version 1.8

### [1.2] Client setup ###
For the client to be able to encrypt and decrypt files the Unlimited Strength JCE is required. To set it up:

* Go to [this](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) link and download the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8
* Go to jdk1.8 path under *"./Contents/home/jre/lib/security/"* (for OSX machines is normally *"/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/lib/security/"*)
* Extract the contents of the downloaded file and replace the existing ones 

Note: Remember to keep a backup of the old files

## [2] Execute the app ##
**To start the server and the client:**

* Have a directory containing the `Cryptostore-server.jar` and the certificate (`mySrvKeystore`)
* Have a directory containing the `Cryptostore-client.jar` and the certificate (`mySrvKeystore`)
* Open two terminal windows pointing to each of the directories
* From the terminal pointing to the server dir do: `java -jar Cryptostore-server.jar`
* From the terminal pointing to the server dir do: `java -jar Cryptostore-client.jar`

**To login:**

On the database there are currently 2 accounts to use.

* To log as an administrator use the following credentials:
	* Username: `Admin1`
	* Password: `Ch@ngeM3`
	* Encryption Password: `Ch@ngeM3`
* To log as a User use the following credentials:
	* Username: `User1`
	* Password: `Ch@ngeM3`
	* Encryption Password: `Ch@ngeM3`

Disclaimer: Do not use such passwords and never use the same password for login and encryption.


## [3] Steps to compile file ##
Note: The application points by default to the `localhost` and not the AWS instance.

### [3.1] To change the location (IP of the server) which the client is pointing at: ###

* Open the intelliJ project
* Go to `Cryptostore-client/src/com/cryptostore/client/ClientManager.java`
* Change the `HOST` value (line 22: `public static String HOST = "localhost";`) as required

Note: The AWS instance is assigned a new IP address every time is restarted.

### [3.2] To compile the code from the intelliJ project: ###

* Open the project you wish to compile with the **intelliJ IDEA** (tested with v2016.1)
* If you wish to run the project from the IDE just run the main method of each project
* Otherwise, go to `Build -> Build artifacts... -> {projectName}.jar -> Build` and the generated jar file will be outputted under `./out/artifacts/project_name_jar/project-name.jar`
* To run the jar file follow the instruction from section 1


## [4] Developer-only section ##

### [4.1] To push jar to server ###
Open a cmd from the jar directory and do:
`scp -i {path/to/.pem/file} file.jar ubuntu@{serverIP}:/home/ubuntu/appDir`

### [4.2] To SSH to server ###
`ssh -v -i {path/to/.pem/file} ubuntu@{serverIP}`

### [4.3] This is only to reset the database if accidentally gets dropped ###
To setup database and add a default admin with Username "Admin1", password "Ch@ngeM3" and encryption password "Ch@ngeM3" run the following query:

CREATE DATABASE cs391;


```
#!SQL

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
	('Admin1', '22E635FDCC708F8FEC7A9DB368480CFE1EA9B43F0DFAF85989D82F408F57509C', 'HkZSxED+uTdX2ZdESm9nsQ==', '1753D014F47533E4F4CCC471023325BBBE2A69F6D93C8C6D98A4AE6CEF4D4335', 'e3xFusKT1TliZyR6HhQdNA==', 1);
```

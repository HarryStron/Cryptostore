# CryptoStore #

CryptoStore is a solution for securing sensitive files on remote servers. CryptoStore is a zero-knowledge cloud storage application which encrypts your files locally (on your machine) and then sends them over to the remote server which has no knowledge of what the files stored on it are. The files are only decrypted when they reach back to their owner as no passwords are stored anywhere outside the user's machine.

### What is this repository for? ###

* This is the main repository for the development of CryptoStore
* It contains information on:
  * How to download/build the application
  * How to set up the environment
  * How to install the application
  * How to use the application

### How do I get set up? ###

#### Server ####
* Set up MySQL (full instructions [here](http://www3.ntu.edu.sg/home/ehchua/programming/sql/MySQL_HowTo.html))
  * download and install [MySQL](http://dev.mysql.com/downloads/mysql/:) (full instructions [here](http://www3.ntu.edu.sg/home/ehchua/programming/sql/MySQL_HowTo.html))
  * Install a [JDBC Driver](http://dev.mysql.com/downloads/connector/j/)
  * Copy the JAR file "mysql-connector-java-5.1.{xx}-bin.jar" to JDK's extension directory at "/Library/Java/Extension".
  * 

#### Client ####

### Who do I talk to? ###

Contact e-mail: Harry.Strongyloudis@protonmail.com
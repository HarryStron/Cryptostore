# Tickets
This document is going to contain all the branches and descriptions about why they exist. The branches are going to be listed in 3 categories: Features, BUGs and Other. The (##) will define the categories, the (###) the name of the branch and under the name there would be a description on what is required to be done on the branch.

The file should be updated every time a new branch is created!

## Features

**`feature-1`**:<br/>
Set up a database and create appropriate class for basic communication/manipulation of it. Include validators to check the input of the credentials.<br/>
**_v1.1.0_**

**`feature-2`**:<br/>
Create a new class to hash the passwords.<br/>
**_v1.2.0_**

**`feature-3`**:<br/>
Update JDBCControl class to store/retrieve the salts. Create a method to get existing usernames.<br/>
**_v1.3.0_**

**`feature-4`**:<br/>
Make server require client authentication on connect and code client to send the credentials on AUTH message received.<br/>
**_v1.4.0_**

**`feature-5`**:<br/>
Validate all user input on the server side to prevent attacks.<br/>
**_v1.5.0_**

**`feature-6`**:<br/>
Place files of each user into its personal directory.<br/>
**_v1.6.0_**

**`feature-7`**:<br/>
Write the encryption class and all its methods.<br/>
**_v1.7.0_**

**`feature-8`**:<br/>
Encrypt all files and filenames before sending them and decrypt them when they are returned back from server.<br/>
**_v1.8.0_**

**`feature-9`**:<br/>
Allow user to have sub-directories in the server's main user direcory.<br/>
**_v1.9.0_**

**`feature-10`**:<br/>
Create a mapping to represend filenames in such a way that they do not reveal anything about their contents and use it to send and receive files. ATTENTION: this will make the server side store everything as a flat directory!<br/>
**_v1.10.0_**

**`feature-11`**:<br/>
Encrypt the file storing the hashmap. Pull it when the client is started and push it after sending a file to the server.<br/>
**_v1.11.0_**

**`feature-12`**:<br/>
Implement deleting of a remote file.<br/>
**_v1.12.0_**

**`feature-13`**:<br/>
Create a syncronization class and methods to SYNC files between client and server.<br/>
**_v1.13.0_**

**`feature-14`**:<br/>
Create a basic but working UI using JavaFX.<br/>
**_v1.14.0_**

**`feature-15`**:<br/>
Allow client to upload and delete directories.<br/>
**_v1.15.0_**

**`feature-16`**:<br/>
Implement steganographic class.<br/>
**_v1.15.0_**

## BUGs

**`bug-1`**:<br/>
No message is send over to the server (i.e. ERROR msg) when one of the client transfers fail.<br/>
**_v1.4.1_**

**`bug-2`**:<br/>
No exception messages are printed. Need to be displayed for ease of understanding why a problem occured.<br/>
**_same as branch: bug-1_**

**`bug-3`**:<br/>
The client connects to the server and starts the sending process without encrypting the file before hand. For large files that take time to be encrypted that might cause problems such as timeouts. Also not worth taking up resources such as sockets listening for no particular reason. Encrypt before sending.<br/>
**_v1.9.1_**

**`bug-4`**:<br/>
The `handleError()` method at the ClientManager gets into an infinite recursive call if the transferManager instance is not initiated when handling an error. Make sure it is initiated before trying to send an error message to the server.<br/>
**_v1.9.2_**

**`bug-5`**:<br/>
The file output stream is not closed after writing the file bytes.<br/>
**_v1.9.3_**

**`bug-6`**:<br/>
Failure to transmit files bigger than a few MBs long. Split the data into chunks and send separately.<br/>
**_v1.9.4_**

**`bug-7`**:<br/>
Change hashmaps to a biderectional data type. It is needed by server for deletion of files.<br/>
**_v1.11.1_**

**`bug-8`**:<br/>
On file deletion, filemap entries are deleted prior to sync-file updating causing null pointers because the filenames cannot be translated to their encrypted form. Updating of sync-file must happen before the corresponding filename entry is deleted from the file map.<br/>
**_v1.13.1_**

## Other (_not tagged_)

**`other-1`**:<br/>
Create a new README file needed by GitBucket to hold all basic information.<br/>
**_merged_**

**`other-2`**:<br/>
Refactoring needed to clean up code. Also handle everything using exceptions to make the code more readable with less IF statements.<br/>
**_merged_**

**`other-3`**:<br/>
Change OK, READY and DONE commands to a single command to represent all 3. This reduces the code and makes it easier for errors and exceptions to handle.<br/>
**_merged_**

**`other-4`**:<br/>
Create basic tests for SYNC, Upload, Download and Delete.<br/>
**_merged_**

**`other-5`**:<br/>
Refactoring.<br/>
**_merged_**

**`other-6`**:<br/>
Refactoring. Replace printf with <br/>
**_merged_**


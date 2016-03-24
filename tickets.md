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
Implement steganography class.<br/>
**_v1.16.0_**

**`feature-17`**:<br/>
Hide files in images before sending and retrieve them on download when steganography is enabled. Never hide the MAP and the SYNC file within images.<br/>
**_v1.17.0_**

**`feature-18`**:<br/>
Do not allow the same user to log in multiple times at the same time.<br/>
**_v1.19.0_**

**`feature-19`**:<br/>
Add a popup window to provide feedback to the user when an action fails.<br/>
**_v1.18.0_**

**`feature-20`**:<br/>
Keep a hash of the decryption pass in the DB to verify it on login.<br/>
**_v1.20.0_**

**`feature-21`**:<br/>
Create an additional option in the main view, where Admin users will be able to register new users to the database (via server).<br/>
**_v1.21.0_**

**`feature-22`**:<br/>
Allow user to choose the PNG he wishes for the stego mode. Also provide a default for ease.<br/>
**_v1.22.0_**

**`feature-23`**:<br/>
Create a button in the UI to individually push editing made to the selected file to the server (under the same name and dir).<br/>
**_v2.1.0_**

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

**`bug-9`**:<br/>
Sync gets really slow if there are hundreds of files to be checked using hashes. Use version numbers so that the client knows when it is already in sync with the server and can skip the SYNC phase.<br/>
**_v1.15.1_**

**`bug-10`**:<br/>
Client needs to verify that the user is AUTHED before any action. It is also practical to check that the connection channel with the server is still available (use a heartbeat).<br/>
**_v1.17.1_**

**`bug-11`**:<br/>
When registering a user check all fields are complete.<br/>
**_v1.21.1_**

**`bug-12`**:<br/>
Inform user about password/username requirements when trying to register a new user.<br/>
**_v1.21.2_**

**`bug-13`**:<br/>
If file uploaded on stego mode is not going to fit the png then abort action and notify. Also think about the file size in bits fitting the first row of the png pixels.<br/>
**_v1.21.3_**

**`bug-14`**:<br/>
If file exists on server do not allow to re-upload.<br/>
**_v1.21.4_**

**`bug-15`**:<br/>
Refactoring.<br/>
**_v1.21.5_**

**`bug-16`**:<br/>
SteganographyManager is not verifying that file to be concealed fits the image. Also needless loops are done for hiding and retrieving file.<br/>
**_v2.0.1_**

**`bug-17`**:<br/>
Inform user that needs to be admin when he tries to register a new user.<br/>
**_v2.0.2_**

**`bug-18`**:<br/>
Change beaviour of the stego button. Make "on" and "off" change respectivelly to active/inactive.<br/>
**_v2.1.1_**

**`bug-19`**:<br/>
Thread to handle multiple users blocks, resulting multiple users to be impossible to connect in parallel.<br/>
**_v2.1.2_**

**`bug-20`**:<br/>
App does not work from windows machines. Need to make paths platform independent.<br/>
**_v2.1.3_**

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
Make all changes needed for the client-server to run on AWS. That includes: 

- set up linux server on amazon
- change IPs to match server
- make sure libraries and all dependencies are properly extracted with the jar files
- make sure self-signed keys are on the same path with the jars

**_v1.19.0.1_**
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
Server should have different directories for each of the users' files.<br/>
**_queued_**

## BUGs

**`bug-1`**:<br/>
No message is send over to the server (i.e. ERROR msg) when one of the client transfers fail.<br/>
**_ongoing_**

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

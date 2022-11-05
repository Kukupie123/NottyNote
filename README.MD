# Kuku's NottyNote

NottyNote is a cloud-enabled, offline-compatible Note creating application where you can create custom Note layouts and
use it to create notes with a specific layout.<br>
The **Backend** has been built using **microservice** architecture in mind and **Non blocking reactive** framework _(
Webflux)_.
<br> It makes use of **JWT** for **Authentication** and **mongoDb** for storing data.

## Contents

1. Application Details
2. [Backend](#Backend) :
    1. [ Database ](#Database)
    2. [Services](#Services)
    3. FAQ
4. Frontend :
    1. WIP

# 1. Application Details

Register an account and start creating Note Layouts _(Notty Layout)_. We can then make use of this Layout to create
Notes _(NottyNote)_ with the specified layout.
<br>
To organise things we have implemented Directory system which will allow you to organise NottyNotes into folders and
sub-folders. <br>

# Backend

## Database

We use MongoDB which is a NO-SQL database.<br> Given below are the Tables/Collections our database has : <br>

#### USER

Stores User credential and info

|userID | password  | 
|--- | --- | 
|email of the user| the password in encoded form
|anEmail@gmail.com| RandomEncodedPassword

#### Notty Layout

Notty Layouts created by users are stored in this table

|ID | Name  | CreatorID | BookmarksID | struct |
|--- | --- | --- | --- | --- |
|generated ID| Name of the template | userID of the user who created the template | Array of bookmarks ID which were created using this template | The fields described by this layout
|578| Author Template | anEmail@gmail.com | [123,456,789] | ` title : { mandatory : true, type : text}`

#### Notty Note

Notty Notes are stored in this table

| ID | Name | CreatorID | TemplateID | DirID | Data | isPublic
| --- | --- | --- | --- | --- | --- | ---
|Generated ID| The name of the bookmark | userID of the userID who created this Note | The Layout ID which has been used for this bookmark | Directory ID of the dir this bookmark is at | The data of the bookmark. Corresponds to the struct of layout from Notty Layout Collection | true or false to signify if it's a visible to only the user or to everyone |
| 789 | Bible | anEmail@gmail.com | 578 | 619 | ```{title : Easy to read version}``` | false|

#### Directory

Folders and sub-folders are stored in this table

| ID | Name | CreatorID | ParentID | ChildrenIDs | BookmarkIDs | 
| ---|---|---|---|---|--- |
|Generated ID | Name of the folder | userId of the user who created this directory | directory ID of it's parent (Only valid if it is a sub folder) | Array of Directory IDs who are the children of this Directory | Array of bookmark IDs that are in this directory|
619 | Spiritual Stuff | anEmail@gmail.com | 026 | [784,720,261] | [789,696,001]

#### Connector

This table is used connect Root Directories (Folder which are not Sub Folders i.e No parent), Notty Layouts, Notty Notes
created by the user with their IDs. This table is there to get the IDs of dirs, layouts & notes created by the user very
fast without having to check each table and comparing the CreatorID. Theoretically this should be more efficient.

| ID | Layouts | Notes | Dirs
| --- | --- | --- | --- |
| The userID | Array of Layout IDs that were created by the user | Array of Notes IDs that were created by the user | Array of Dir IDs that were created by the user |
| anEmail@gmail.com | [578] | [789] | [619]

### Services

There are currently 2 services : <br>

### Gateway Service

Gateway service acts as the entry point which is going to be exposed to the client. The client will be talking with
endpoints of this server. <br>
Gateway Service also acts as a security filter. It authenticates users and only allow requests to pass through if it has
been authenticated. Every endpoint of gateway service is closed behind authentication, only login and register endpoint
are open.

### Database Service

Every interaction with the database is done by this service. <br> Registering user, Validating Login Credentials,
Creating Notty Notes are some examples of database interaction <br>
Database Service has to take care of making sure every table has their data in sync during creation, modification and
deletion. <br>
To understand it we can take an example where  : <br>
**User has deleted a Notty Layout.**

- The service will need to get all the Notty Notes created with that Layout and delete them.
- It will then need to access the directories which had those bookmark IDs and remove them.
- And only then, the Notty layout will be finally deleted.
# moosic
Social music sharing app

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Social music app (playing music, playlists, finding songs) where you can see what other people are listening to live

### App Evaluation
- **Category:** Entertainment/Social Networking
- **Mobile:** Easier to use to connect with other people through music  
- **Story:** Allows users to find songs, build playlists, and see what their friends are listening to
- **Market:** Anyone who listens to music
- **Habit:** People listen to music throughout the day when they run errands / work out / etc, so it will be habit forming
- **Scope:** Can start out initially as a music-focused app with traditional music app capabilities (find songs, create/edit playlists, like/unlike songs, shuffle/play/pause) and an instagram-style social feed of friends and the music they are listening to

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can login
* User can search for songs
* User can like/unlike songs
* User can create a playlist and add/delete songs from it 
* User can view their library of liked songs + playlists
* Users can add other users as friends
* User can view the songs that their friends are currently playing
* User can comment on their friends' music 
* User can play/pause/skip songs 
* User can shuffle playlists
* User can add tags to playlists and filter playlists by tag
* ...

**Optional Nice-to-have Stories**

* User can receive song reccomendations based on songs they have listened to in the past / songs in a given playlist 
* User can receive playlist reccomendations 
* User can be connected with other users with similar music tastes 
* User can add songs to a queue
* User can download/share songs 
* User can see a progress bar for the song as it plays
* User can scrub the progress bar to skip to different places in the song 
* User can view other users' pages (including artists) 
* Users can be matched with people with similar music tastes near them (geo-location)
* User can adjust the radius of people nearby them / filter by songs/playlists/artists of those people
* Users can see what people nearby them are listening to
* Threading for playing music / improving performance
* Faster indexing/querying for searching songs (biased toward recent songs searched / similar songs?)
* Offline database integration (SQL) for some caching
* Partial word completion when searching for songs
* See hot/trending songs 
* ...

### 2. Screen Archetypes

* Login screen 
   * User can login
* Home feed screen
   * User can see the songs their friends are currently listening to
* Friend music screen
   * User can see the song that their friend is currently listening to and comment 
* Search song/playlist/user screen
   * User can search for songs
   * User can filter playlists by tag
* User library screen 
   * User can view their library of liked songs + playlists
* Playlist screen 
   * User can like/unlike songs
   * User can play/pause/skip songs 
   * User can shuffle playlists
   * User can add tags to playlists 
   * User can edit playlist 
* Playlist creation screen
   *  User can create a playlist and add/delete songs from it 
* Song screen 
   * User can like/unlike songs
   * User can play/pause/skip songs 
   * User can add song to playlists 
* Song queue screen
   * User can see songs they have queued up
   * User can edit the queue
* Other user screen
   * User can view the library of another user 
* Queue Screen
   * User can see the songs they have queued up 
* Reccomended songs/playlists/users screen
* Nearby users (map?) screen
* Trending / friends screen 

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home Feed
* Search
* Queue
* Library 

**Flow Navigation** (Screen to Screen)

* Login Screen
    * => Home Feed
* Registration Screen
    * => Home Feed
* **Home Feed Screen**
    * => User's current music screen 
    * => User's current music chat screen 
    * => Song Screen
    * => Playlist Screen 
* **Search Screen**
    * => Song Screen
    * => Playlist Screen 
    * => User Screen
    * => Song List Screen
    * => Playlist List Screen 
    * => User List Screen
* **Queue Screen**
    * => Song Screen 
* **Library Screen**
    * => Song List Screen 
    * => Playlist List Screen
    * => User List Screen 
* User's current music screen
    * => User's current music chat screen 
    * => Song Screen
    * => Playlist Screen 
* Playlist Screen 
    * => Song Screen 
    * => Edit Playlist Screen 
* Playlist List Screen
    * => Playlist Screen
* Edit Playlist Screen 
    * => Search Screen 
    * => Playlsit Screen 
* Song Screen 
* Song List Screen 
    * => Song Screen
* User Screen 
    * => Song Screen
    * => Playlist Screen 
    * => User Screen
    * => Song List Screen
    * => Playlist List Screen 
    * => User List Screen
* User List Screen
    * => User Screen

## Wireframes
![wireframe](https://user-images.githubusercontent.com/59301744/173682535-59e119d1-6a81-49d1-9345-c463430172e5.jpeg)

## TODO: Schema 
[This section will be completed in Unit 9]
### TODO: Models
[Add table of models]
### TODO: Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]

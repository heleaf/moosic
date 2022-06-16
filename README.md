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

## Schema 
### Models
#### Status
   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user status (default field) |
   | author        | Pointer to User| status author |
   | song          | Pointer to Song | song that the author is currently listening to |
   | playlist      | Pointer to Playlist | playlist that the author is currently listening to | 
   | caption       | String   | status caption by author |
   | commentsCount | Number   | number of comments that has been posted to a status |
   | likesCount    | Number   | number of likes for the status |
   | createdAt     | DateTime | date when status is created (default field) |
   | updatedAt     | DateTime | date when status is last updated (default field) |
#### Song
   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the song (default field) |
   | name          | String   | name of the song |
   | artist        | Pointer to User | song artist |
   | artists       | Array<User> | list of song artists | 
   | album         | Pointer to Album | album that the song belongs to |
   | timeLength    | Number | length of song in miliseconds |
   | playCount     | Number | number of times the song has been played |
   | likesCount    | Number | number of users who have liked the song |
   | isLikedByUser | Boolean | whether the song is liked by the user | 
   | isQueued      | Boolean | whether the song is currently queued |
   | isPlaying     | Boolean | whether the song is currently playing |
   | playPosition  | Number | number of seconds that have passed in the song | 
   | tags          | Array<String> | relevant keywords that the song will show up for when querying | 
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |
 
#### Playlist 
   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the playlist (default field) |
   | name          | String   | name of the playlist |
   | author        | Pointer to User | author of the playlist |
   | caption       | String   | playlist caption by the author |
   | songs         | Array<Song> | list of songs in the playlist | 
   | timeLength    | Number | length of the entire playlist in miliseconds |
   | isLikedByUser | Boolean | whether the playlist is liked by the user | 
   | playCount     | Number | number of times the song has been played |
   | likesCount    | Number | number of users who have liked the song |
   | tags          | Array<String> | relevant keywords that the playlist will show up for when querying |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |

#### User
   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user (default field) |
   | username      | String   | username of the user |
   | password      | String   | password of the user | 
   | likedSongs    | Array<Song> | list of liked songs by the user | 
   | likedPlaylists | Array<Playlist> | list of liked playlists by the user |
   | isFollowedByUser | Boolean | whether the user is followed by the currentUser | 
   | tags          | Array<String> | relevant keywords that the user will show up for when querying | 
   | isOnline      | Boolean | whether the user shows up on their followers' feed | 

### Networking
#### Main Activity 
- (Create/POST) Create a new status object
- (Update/PUT) Update an existing status object
- (Delete) Delete status object 
  
#### Home Feed Screen
- (Read/GET) Query all statuses from the current user's online following list 
   ```Java
    // get an object for querying posts
        ParseQuery<Status> query = ParseQuery.getQuery(Status.class);

        // include data referred by user key
        query.include(Status.KEY_USER);
 
        query.whereContainedIn("objectId", ParseUser.getCurrentUser().getFollowingList())

        query.addDescendingOrder(Status.KEY_CREATED_AT); // newest first

        query.findInBackground(new FindCallback<Status>() {
            @Override
            public void done(List<Status> objects, ParseException e) {
                if (e != null){
                    // TODO: error
                }
                // TODO: do something 
            }
        });
   ```

- (Create/POST) Create a new like on a song, playlist, or user 
- (Update/PUT) Add a song to a playlist 
- (Delete) Delete existing like on a song, playlist, or user
  
#### Search Screen
- (Read/GET) Query lists of songs, playlists, users by searched keywords
- (Create/POST) Create a new like on a song, playlist, or user 
- (Update/PUT) Add a song to a playlist
- (Delete) Delete existing like on a song, playlist, or user

### Profile Screen 
- (Read/GET) Query lists of liked songs, playlists, and users 
- (Read/GET) Query logged in user object 
 
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]

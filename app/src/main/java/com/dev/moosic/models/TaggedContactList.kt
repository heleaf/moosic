package com.dev.moosic.models

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import com.parse.ParseQuery
import com.parse.ParseRelation
import com.parse.ParseUser
import org.parceler.Parcel
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import retrofit.mime.TypedString

@Parcel
class TaggedContactList() {
    var contactList: ArrayList<Pair<Contact, String>> = ArrayList()
    var followedFriendsStartIndex = 0
    var friendsYouMightKnowStartIndex = 0
    var recommendedFriendsStartIndex = 0

    val DEFAULT_NUMBER_RECOMMENDED_USERS = 3

    var contentResolver: ContentResolver? = null

    val TAG = "TaggedContactList"
    val KEY_USERS_FOLLOWED = "usersFollowed"
    val KEY_USER_PHONE_NUMBER = "phoneNumber"

    val dummyResponse = Response( // TODO: adjust to be actual response
        "url", 200,
        "reason", emptyList(),
        TypedString("string")
    )

    fun addToFollowedFriends(contact: Contact) {
        val index = getIndexForNewContact(contact.parseUsername!!,
            followedFriendsStartIndex, friendsYouMightKnowStartIndex)
        contactList.add(index, Pair(contact, Contact.KEY_FOLLOWED_CONTACT))
        friendsYouMightKnowStartIndex += 1
        recommendedFriendsStartIndex += 1
    }

    // TODO: change to binary search
    private fun getIndexForNewContact(parseUsername: String,
                                      startIndexIncl: Int,
                                      endIndexExcl: Int): Int {
        for (i in startIndexIncl until endIndexExcl) {
            val contact = contactList.get(i)
            if (contact.first.parseUsername!! > parseUsername) {
                return i
            }
        }
        return endIndexExcl
    }

    fun asyncFillTaggedContactList(callback: Callback<Unit>) {
        val phoneContacts = getContacts()
        asyncFilterFriendsFromContacts(phoneContacts,
            object: Callback<Pair<List<Contact>, List<Contact>>> {
                override fun success(t: Pair<List<Contact>, List<Contact>>?, response: Response?) {
                    if (t == null) {
                        callback.failure(retrofit.RetrofitError.unexpectedError("no url",
                            Throwable("non friends and friends are null")))
                        return
                    }
                    val nonFriendParseUsers = t.first
                    val friendParseUsers = t.second

                    val taggedNotAddedContacts = nonFriendParseUsers.map{
                            contact -> Pair(contact, Contact.KEY_NOT_FOLLOWED_CONTACT)
                    }
                    val taggedFollowedFriends = friendParseUsers.map{
                            contact -> Pair(contact, Contact.KEY_FOLLOWED_CONTACT)
                    }

                    asyncGetRecommendedFriends(false,
                        friendParseUsers, DEFAULT_NUMBER_RECOMMENDED_USERS,
                        object: Callback<List<Pair<Contact, Double>>> {
                            override fun success(
                                recs: List<Pair<Contact, Double>>?,
                                response: Response?
                            ) {
                                if (recs == null) {
                                    callback.failure(retrofit.RetrofitError.unexpectedError("no url",
                                        Throwable("recommended users are null")))
                                    return
                                }
                                val taggedRecommendedFriends = recs.map {
                                        pair -> val contact = pair.first;
                                    contact.similarityScore = pair.second;
                                    Pair(contact, Contact.KEY_RECOMMENDED_CONTACT)
                                }

                                val pairComparator = Comparator {
                                        friend1 : Pair<Contact, String>,
                                        friend2 : Pair<Contact, String> ->
                                    friend1.first.parseUsername!!.compareTo(friend2.first.parseUsername!!, ignoreCase = true)
                                }

                                val sortedFollowedFriends = taggedFollowedFriends.sortedWith(pairComparator)
                                val sortedNotAddedContacts = taggedNotAddedContacts.sortedWith(pairComparator)
                                val sortedRecommendedFriends = taggedRecommendedFriends.sortedWith(pairComparator)

                                followedFriendsStartIndex = 0
                                contactList.addAll(sortedFollowedFriends)

                                friendsYouMightKnowStartIndex = contactList.size
                                contactList.addAll(sortedNotAddedContacts)

                                recommendedFriendsStartIndex = contactList.size
                                contactList.addAll(sortedRecommendedFriends)

                                callback.success(Unit, response)
                            }
                            override fun failure(error: RetrofitError?) {
                                callback.failure(error)
                            }
                        })
                }
                override fun failure(error: RetrofitError?) {
                    callback.failure(error)
                }
            })
    }

    fun clearTaggedContactList() {
        contactList.clear()
        followedFriendsStartIndex = 0
        friendsYouMightKnowStartIndex = 0
        recommendedFriendsStartIndex = 0
    }

    private fun getContacts(): ArrayList<Contact> {
        val phoneContacts : ArrayList<Contact> = ArrayList()
        val resolver: ContentResolver = contentResolver!!
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null,
            null)
        if (cursor!!.count > 0) {
            while (cursor.moveToNext()) {
                val contact = Contact()

                val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneNumberIdx = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                val id = cursor.getString(idIdx)
                val name = cursor.getString(nameIdx)
                val phoneNumber = (cursor.getString(hasPhoneNumberIdx)).toInt()

                if (phoneNumber > 0) {
                    val cursorPhone = contentResolver!!.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                    if(cursorPhone!!.count > 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneIdx = cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            val phoneNumValue = cursorPhone.getString(phoneIdx)
//                            builder.append("Contact: ").append(name).append(", Phone Number: ").append(
//                                phoneNumValue).append("\n\n")
                            Log.e("Name ===>",phoneNumValue);
                            Log.d(TAG, "name: " + name + " phone number: " + phoneNumValue)
                            contact.name = name
                            // parse this to lose the parens, extra spaces, and dashes
                            val cleanedUpPhoneNumValue =
                                String.format("%s%s%s",
                                    phoneNumValue.slice(IntRange(1,3)),
                                    phoneNumValue.slice(IntRange(6,8)),
                                    phoneNumValue.slice(IntRange(10,13))
                                )
                            contact.phoneNumber = cleanedUpPhoneNumValue
                        }
                    }
                    cursorPhone!!.close()
                }

                val emailCursor = contentResolver!!.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", arrayOf(id), null)
                if (emailCursor != null) {
                    while (emailCursor.moveToNext()) {
                        val emailIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                        val email = emailCursor.getString(emailIdx)
                        Log.d(TAG, "email extracted: " + email)
                        contact.email = email
                    }
                    emailCursor.close()
                }
                phoneContacts.add(contact)
            }
        } else {
            //   toast("No contacts available!")
        }
        cursor.close()
        return phoneContacts
    }

    private fun asyncFilterFriendsFromContacts(contactList: List<Contact>,
                                       callback: Callback<Pair<List<Contact>, List<Contact>>>) {
        val usersFollowedRelation = ParseUser.getCurrentUser().getRelation<ParseUser>(
            KEY_USERS_FOLLOWED
        )
        asyncGetNonFriendParseUsers(contactList, 0, ArrayList<Contact>(), usersFollowedRelation, object: Callback<List<Contact>> {
            override fun success(t: List<Contact>?, response: Response?) {
                if (t != null) {
                    val nonFriendParseUsers = ArrayList<Contact>()
                    nonFriendParseUsers.addAll(t)
                    val friendParseUserQuery = usersFollowedRelation.query
                    friendParseUserQuery.findInBackground { friendParseUsers, e ->
                        if (e != null) {
                            val throwable = Throwable(e.message.toString())
                            val error = retrofit.RetrofitError.unexpectedError(
                                "no url", throwable
                            )
                            callback.failure(error)
                        }
                        else if (friendParseUsers == null) {
                            val throwable = Throwable("friends found are null")
                            val error = retrofit.RetrofitError.unexpectedError(
                                "no url", throwable
                            )
                            callback.failure(error)
                        }
                        val friendParseUserContacts = friendParseUsers.map{parseUser -> Contact.fromParseUser(parseUser)}
                        callback.success(Pair(nonFriendParseUsers, friendParseUserContacts), dummyResponse)
                    }
                }
            }
            override fun failure(error: RetrofitError?) {
                Log.e(TAG, error?.message.toString())
            }
        })
    }


    private fun asyncGetNonFriendParseUsers(contactList: List<Contact>,
                                            index: Int,
                                            accumulator: ArrayList<Contact>,
                                            usersFollowedRelation: ParseRelation<ParseUser>,
                                            callback: Callback<List<Contact>>) {
        if (index >= contactList.size) {
            callback.success(accumulator, dummyResponse)
        }
        else if (index < 0) {
            val throwable = Throwable("index into list of contacts is negative")
            val error = retrofit.RetrofitError.unexpectedError(
                "no url", throwable
            )
            callback.failure(error)
        } else {
            val contact = contactList.get(index)
            val usersFollowedQuery = usersFollowedRelation.query
            usersFollowedQuery.whereEqualTo(KEY_USER_PHONE_NUMBER, contact.phoneNumber)
            usersFollowedQuery.findInBackground { usersMatched, err ->
                if (err != null) {
                    val throwable = Throwable(err.message.toString())
                    val error = retrofit.RetrofitError.unexpectedError(
                        "no url", throwable
                    )
                    callback.failure(error)
                }
                else if (usersMatched == null) {
                    val throwable = Throwable("objects found are null")
                    val error = retrofit.RetrofitError.unexpectedError(
                        "no url", throwable
                    )
                    callback.failure(error)
                }
                else if (usersMatched.size > 0) {
                    // already a friend
                    asyncGetNonFriendParseUsers(contactList,
                        index+1,
                        accumulator,
                        usersFollowedRelation,
                        callback)
                } else {
                    // not a friend, but might be a parse user
                    val query = ParseQuery.getQuery(ParseUser::class.java)
                    query.whereEqualTo(KEY_USER_PHONE_NUMBER, contact.phoneNumber)
                    query.findInBackground { objects, e ->
                        if (e != null) {
                            val throwable = Throwable(e.message.toString())
                            val error = retrofit.RetrofitError.unexpectedError(
                                "no url", throwable
                            )
                            callback.failure(error)
                        }
                        else if (objects == null) {
                            val throwable = Throwable("objects found are null")
                            val error = retrofit.RetrofitError.unexpectedError(
                                "no url", throwable
                            )
                            callback.failure(error)
                        }
                        else if (objects.size > 0){
                            // is a parse user
                            contact.parseUsername = objects.get(0).username
                            contact.parseUserId = objects.get(0).objectId
                            accumulator.add(contact)
                            asyncGetNonFriendParseUsers(contactList,
                                index+1,
                                accumulator,
                                usersFollowedRelation,
                                callback)
                        }
                        else {
                            // not a parse user
                            asyncGetNonFriendParseUsers(contactList,
                                index+1,
                                accumulator,
                                usersFollowedRelation,
                                callback)
                        }
                    }
                }
            }
        }
    }


    private fun asyncGetRecommendedFriends(includeCurrentUser: Boolean,
                                   contactsToIgnore: List<Contact>,
                                   numberOfUsersRequested: Int,
                                   callback: Callback<List<Pair<Contact, Double>>>) {
        // prepare the query of users
        val userQuery = ParseUser.getQuery()
        val usernamesToIgnore = ArrayList<String>()
        if (!includeCurrentUser) {
            usernamesToIgnore.add(ParseUser.getCurrentUser().username)
        }
        for (contact in contactsToIgnore) {
            contact.parseUsername?.let { usernamesToIgnore.add(it) }
        }
        userQuery.whereNotContainedIn("username", usernamesToIgnore)
        userQuery.findInBackground { objects, e ->
            if (e != null) {
                val throwable = Throwable(e.message.toString())
                val error = retrofit.RetrofitError.unexpectedError(
                    "no url", throwable
                )
                callback.failure(error)
            }
            else if (objects == null) {
                val throwable = Throwable("objects found are null")
                val error = retrofit.RetrofitError.unexpectedError(
                    "no url", throwable
                )
                callback.failure(error)
            }
            asyncGetInterestVectorOfUserList(objects!!, 0,
                ArrayList<Pair<Contact, Map<String, Double>>>(),
                object: Callback<List<Pair<Contact, Map<String, Double>>>> {
                    override fun success(
                        interestVectors: List<Pair<Contact, Map<String, Double>>>?,
                        response: Response?
                    ) {
                        // do my calculations here for similarity
                        if (interestVectors == null) {
                            val throwable = Throwable("null interest vectors")
                            callback.failure(retrofit.RetrofitError.unexpectedError("no url", throwable))
                            return
                        }
                        SongFeatures.asyncGetUserPlaylistFeatureMap(ParseUser.getCurrentUser(), object: Callback<Map<String, Double>> {
                            override fun success(userInterestVector: Map<String, Double>?, response: Response?) {
                                if (userInterestVector == null) {
                                    val throwable = Throwable("null interest vectors")
                                    callback.failure(retrofit.RetrofitError.unexpectedError("no url", throwable))
                                    return
                                }
                                val interestVectorSimilarities : List<Pair<Contact, Double>>
                                        = interestVectors.map {
                                        userVectorPair ->
                                    Pair(userVectorPair.first,
                                        computeVectorSimilarityScore(userVectorPair.second, userInterestVector))
                                }
                                val similaritiesFilteredOutNaNs = interestVectorSimilarities.filter {
                                        it -> !it.second.isNaN()
                                }
                                val vectorComparator = Comparator {
                                        vec1 : Pair<Contact, Double>,
                                        vec2 : Pair<Contact, Double> ->
                                    if (vec1.second - vec2.second > 0.0) 1
                                    else if (vec1.second - vec2.second == 0.0) 0
                                    else -1
                                }
                                val sortedVectors = similaritiesFilteredOutNaNs.sortedWith(vectorComparator).reversed()
                                val vecsToReturn = if (sortedVectors.size < numberOfUsersRequested) sortedVectors
                                else sortedVectors.slice(IntRange(0, numberOfUsersRequested - 1))
                                callback.success(vecsToReturn, dummyResponse)
                            }
                            override fun failure(error: RetrofitError?) {
                                callback.failure(error)
                            }
                        })
                    }
                    override fun failure(error: RetrofitError?) {
                        callback.failure(error)
                    }
                })
        }
    }


    private fun asyncGetInterestVectorOfUserList(parseUsers: List<ParseUser>,
                                                 index: Int, interestVectorsAccumulator:
                                                 ArrayList<Pair<Contact, Map<String, Double>>>,
                                                 callback: Callback<List<Pair<Contact, Map<String, Double>>>>) {
        if (index >= parseUsers.size) {
            callback.success(interestVectorsAccumulator, dummyResponse)
        }
        else if (index < 0) {
            val throwable = Throwable("index is negative")
            val error = retrofit.RetrofitError.unexpectedError(
                "no url", throwable
            )
            callback.failure(error)
        } else {
            val user = parseUsers.get(index)
            SongFeatures.asyncGetUserPlaylistFeatureMap(user, object: Callback<Map<String,Double>> {
                override fun success(t: Map<String, Double>?, response: Response?) {
                    if (t != null) {
                        interestVectorsAccumulator.add(Pair(Contact.fromParseUser(user), t))
                        asyncGetInterestVectorOfUserList(parseUsers, index+1, interestVectorsAccumulator, callback)
                    }
                }
                override fun failure(error: RetrofitError?) {
                    callback.failure(error)
                }
            })
        }
    }

    private fun computeVectorSimilarityScore(vec1: Map<String, Double>, vec2: Map<String, Double>): Double {
        return dot(vec1, vec2) / (magnitude(vec1) * magnitude(vec2))
    }

    private fun dot(vec1: Map<String, Double>, vec2: Map<String, Double>): Double {
        var total = 0.0
        for (feature in SongFeatures.FEATURE_KEYS_ARRAY){
            val v1Entry = vec1.getOrDefault(feature, 0.0)
            val v2Entry = vec2.getOrDefault(feature, 0.0)
            if (v1Entry.isNaN() || v2Entry.isNaN()) {
                Log.d(TAG, "NANs found")
            } else {
                total += v1Entry * v2Entry
            }
        }
        return total
    }

    private fun magnitude(vec: Map<String, Double>): Double {
        var total = 0.0
        for (feature in SongFeatures.FEATURE_KEYS_ARRAY) {
            val entry = vec.getOrDefault(feature, 0.0)
            if (!entry.isNaN()) {
                total += entry * entry
            }
        }
        return Math.sqrt(total)
    }

}
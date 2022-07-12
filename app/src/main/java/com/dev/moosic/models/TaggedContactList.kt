package com.dev.moosic.models

import org.parceler.Parcel

@Parcel
class TaggedContactList() {
    var contactList: ArrayList<Pair<Contact, String>> = ArrayList()
    var followedFriendsStartIndex = 0
    var friendsYouMightKnowStartIndex = 0
    var recommendedFriendsStartIndex = 0

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


}
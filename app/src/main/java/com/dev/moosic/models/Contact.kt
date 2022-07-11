package com.dev.moosic.models

import com.parse.ParseUser
import org.parceler.Parcel

@Parcel
class Contact() {
    var name : String? = null
    var parseUsername : String? = null
    var email : String? = null
    var phoneNumber : String? = null
    var similarityScore : Double? = null

    companion object Factory {
        val KEY_PHONE_NUMBER = "phoneNumber"
        val KEY_NOT_FOLLOWED_CONTACT = "notFollowed"
        val KEY_FOLLOWED_CONTACT = "followed"
        val KEY_RECOMMENDED_CONTACT = "recommended"
        fun fromParseUser(user: ParseUser): Contact {
            val contact = Contact()
            contact.parseUsername = user.username
            contact.email = user.email
            contact.phoneNumber = user.getString(KEY_PHONE_NUMBER)
            return contact
        }
    }

}
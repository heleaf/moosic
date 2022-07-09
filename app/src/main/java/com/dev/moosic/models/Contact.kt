package com.dev.moosic.models

import com.parse.ParseUser
import org.parceler.Parcel

@Parcel
class Contact() {
    var name : String? = null
    var parseUsername : String? = null
    var email : String? = null
    var phoneNumber : String? = null

    companion object Factory {
        val KEY_PHONE_NUMBER = "phoneNumber"
        fun fromParseUser(user: ParseUser, contact: Contact){
            contact.parseUsername = user.username
            contact.email = user.email
            contact.phoneNumber = user.getString(KEY_PHONE_NUMBER)
        }
    }

}
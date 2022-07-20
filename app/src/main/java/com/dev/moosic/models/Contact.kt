package com.dev.moosic.models

import android.os.Parcelable
import com.parse.ParseUser
import kotlinx.parcelize.Parcelize
import org.parceler.Parcel
import java.io.Serializable

@Parcel
class Contact() : Serializable {
    var name : String? = null
    var parseUsername : String? = null
    var email : String? = null
    var phoneNumber : String? = null
    var similarityScore : Double? = null
    var parseUserId: String? = null

    companion object Factory {
        const val KEY_PHONE_NUMBER = "phoneNumber"
        const val KEY_NOT_FOLLOWED_CONTACT = "notFollowed"
        const val KEY_FOLLOWED_CONTACT = "followed"
        const val KEY_RECOMMENDED_CONTACT = "recommended"
        fun fromParseUser(user: ParseUser): Contact {
            val contact = Contact()
            contact.parseUsername = user.username
            contact.email = user.email
            contact.phoneNumber = user.getString(KEY_PHONE_NUMBER)
            contact.parseUserId = user.objectId
            return contact
        }
    }

}
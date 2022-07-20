package com.dev.moosic.controllers

import com.dev.moosic.adapters.TaggedContactAdapter
import com.dev.moosic.models.Contact

interface FriendsController {
    fun followContact(contact: Contact, position: Int, adapter: TaggedContactAdapter)
    fun unfollowContact(contact: Contact)
    fun launchDetailView(contact: Contact, animate: Boolean = true)
    fun exitDetailView()
}
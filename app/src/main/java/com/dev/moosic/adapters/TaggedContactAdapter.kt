package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.controllers.FriendsController
import com.dev.moosic.models.Contact

class TaggedContactAdapter (context: Context,
                            contactList: List<Pair<Contact, String>>,
                            private val friendsController: FriendsController
) : RecyclerView.Adapter<TaggedContactAdapter.ViewHolder>() {
    var contactList : List<Pair<Contact, String>> = ArrayList()
    var context : Context

    val adapter = this

    init {
        this.context = context
        this.contactList = contactList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(this.context).inflate(
            R.layout.single_contact_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = this.contactList.get(position)
        holder.bind(contact, position)
    }

    override fun getItemCount(): Int {
        return this.contactList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameField : TextView
        var parseUsernameField : TextView
        var similarityField : TextView
        var followButton : Button

        init {
            nameField = itemView.findViewById(R.id.contactName)
            parseUsernameField = itemView.findViewById(R.id.contactParseUsername)
            similarityField = itemView.findViewById(R.id.contactSimilarityScore)
            followButton = itemView.findViewById(R.id.followContactButton)
        }

        fun bind(contactPair: Pair<Contact,String>, position: Int) {
            nameField.visibility = View.GONE
            if (contactPair.first.parseUsername == null) {
                parseUsernameField.visibility = View.GONE
            } else { parseUsernameField.setText(contactPair.first.parseUsername) }
            followButton.setOnClickListener {
                friendsController.followContact(contactPair.first, position, adapter)
            }
            if (contactPair.second != Contact.KEY_FOLLOWED_CONTACT ) {
                followButton.visibility = View.VISIBLE
            } else {
                followButton.visibility = View.GONE
            }
            similarityField.visibility = View.GONE
            itemView.setOnClickListener{
                friendsController.launchDetailView(contactPair.first)
            }
        }
    }
}
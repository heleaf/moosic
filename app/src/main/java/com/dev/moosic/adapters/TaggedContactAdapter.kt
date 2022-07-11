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
import java.math.BigDecimal
import java.math.RoundingMode

class TaggedContactAdapter (context: Context,
                            contactList: List<Pair<Contact, String>>,
                            friendsController: FriendsController
    ) : RecyclerView.Adapter<TaggedContactAdapter.ViewHolder>() {
    var contactList : List<Pair<Contact, String>> = ArrayList()
    var context : Context
    var friendsController : FriendsController

    val adapter = this

    init {
        this.context = context
        this.contactList = contactList
        this.friendsController = friendsController
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

        val TAG = "TaggedContactAdapter"

        init {
            nameField = itemView.findViewById(R.id.contactName)
            parseUsernameField = itemView.findViewById(R.id.contactParseUsername)
            similarityField = itemView.findViewById(R.id.contactSimilarityScore)
            followButton = itemView.findViewById(R.id.followContactButton)
        }

        fun bind(contactPair: Pair<Contact,String>, position: Int) {
            Log.d(TAG, contactPair.first.parseUsername.toString())
            Log.d(TAG, contactPair.first.name.toString())
            Log.d(TAG, contactPair.first.email.toString())

            if (contactPair.first.name == null) {
                nameField.visibility = View.GONE
            } else { nameField.setText(contactPair.first.name) }

            if (contactPair.first.parseUsername == null) {
                parseUsernameField.visibility = View.GONE
            } else { parseUsernameField.setText(contactPair.first.parseUsername) }

            if (contactPair.second == Contact.KEY_NOT_FOLLOWED_CONTACT) {
                followButton.visibility = View.VISIBLE
                followButton.setOnClickListener {
                    friendsController.followContact(contactPair.first, position, adapter)
                }
            } else {
                // add unfollow button?
                followButton.visibility = View.GONE
            }

            if (contactPair.second == Contact.KEY_RECOMMENDED_CONTACT && contactPair.first.similarityScore != null) {
                similarityField.visibility = View.VISIBLE
                val displayedSimilarity // = (contactPair.first.similarityScore!!*100).toInt()
                     = BigDecimal(contactPair.first.similarityScore!!).setScale(2, RoundingMode.HALF_EVEN)
                similarityField.text = String.format("similarity: $displayedSimilarity")
                followButton.visibility = View.VISIBLE
                followButton.setOnClickListener {
                    friendsController.followContact(contactPair.first, position, adapter)
                }
            } else {
                similarityField.visibility = View.GONE
            }
        }
    }
}
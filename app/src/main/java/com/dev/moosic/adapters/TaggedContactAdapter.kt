package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.models.Contact

class TaggedContactAdapter (context: Context, contactList: List<Pair<Contact, String>>) : RecyclerView.Adapter<TaggedContactAdapter.ViewHolder>() {
    var contactList : List<Pair<Contact, String>> = ArrayList()
    var context : Context

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
        holder.bind(contact)
    }

    override fun getItemCount(): Int {
        return this.contactList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameField : TextView
        var parseUsernameField : TextView
        val TAG = "TaggedContactAdapter"

        init {
            nameField = itemView.findViewById(R.id.contactName)
            parseUsernameField = itemView.findViewById(R.id.contactParseUsername)
        }

        fun bind(contactPair: Pair<Contact,String>) {
            Log.d(TAG, contactPair.first.parseUsername.toString())
            Log.d(TAG, contactPair.first.name.toString())
            Log.d(TAG, contactPair.first.email.toString())

            if (contactPair.first.name == null) {
                nameField.visibility = View.GONE
            } else { nameField.setText(contactPair.first.name) }

            if (contactPair.first.parseUsername == null) {
                parseUsernameField.visibility = View.GONE
            } else { parseUsernameField.setText(contactPair.first.parseUsername) }
        }
    }
}
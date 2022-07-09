package com.dev.moosic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.models.Contact

class ContactAdapter(context: Context, contactList: List<Contact>) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    var contactList : List<Contact> = ArrayList()
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

        init {
            nameField = itemView.findViewById(R.id.contactName)
            parseUsernameField = itemView.findViewById(R.id.contactParseUsername)
        }

        fun bind(contact: Contact) {
            if (contact.name == null) {
                nameField.visibility = View.GONE
            } else nameField.setText(contact.name)
            if (contact.parseUsername == null) {
                parseUsernameField.visibility = View.GONE
            } else parseUsernameField.setText(contact.name)
        }
    }
}
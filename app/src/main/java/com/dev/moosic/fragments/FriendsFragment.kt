package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.RecyclerItemDecoration
import com.dev.moosic.adapters.ContactAdapter
import com.dev.moosic.adapters.TaggedContactAdapter
import com.dev.moosic.models.Contact
import com.dev.moosic.models.Song
import org.parceler.Parcels

private const val ARG_PARAM1 = "contactList"
private const val ARG_PARAM2 = "taggedContactList"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendsFragment : Fragment() {

    val TAG = "FriendsFragment"

    private lateinit var rvContacts : RecyclerView
    private lateinit var adapter : TaggedContactAdapter

    // remove this, use taggedContactList instead
    private var contactList: ArrayList<Contact> = ArrayList()

    private var taggedContactList : ArrayList<Pair<Contact, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contactList = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            taggedContactList = Parcels.unwrap(it.getParcelable(ARG_PARAM2))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment FriendsFragment.
         */
        @JvmStatic
        fun newInstance(contactList : ArrayList<Contact>, taggedContactList: ArrayList<Pair<Contact, String>>) =
            FriendsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(contactList))
                    putParcelable(ARG_PARAM2, Parcels.wrap(taggedContactList))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvContacts = view.findViewById(R.id.rvContacts)

        // TODO, pass taggedContactList instead of contactList
        adapter = TaggedContactAdapter(view.context, taggedContactList) //ContactAdapter(view.context, contactList)
        rvContacts.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        rvContacts.layoutManager = linearLayoutManager

        val recyclerItemDecoration : RecyclerItemDecoration
        = RecyclerItemDecoration(requireContext(), resources.getDimensionPixelSize(R.dimen.header_height),
        false, getSectionCallback(taggedContactList))
        rvContacts.addItemDecoration(recyclerItemDecoration)

//        Log.d(TAG, taggedContactList.size.toString())
//        Log.d(TAG, taggedContactList.get(5).first.parseUsername.toString())
//        Log.d(TAG, taggedContactList.get(5).first.name.toString())
//        Log.d(TAG, taggedContactList.get(5).first.email.toString())
//        Log.d(TAG, taggedContactList.get(3).first.)
    }

    inner class DecorationSectionCallback(taggedContacts: ArrayList<Pair<Contact, String>>) : RecyclerItemDecoration.SectionCallback {
        val taggedContacts: ArrayList<Pair<Contact, String>>
        init {
            this.taggedContacts = taggedContacts
        }
        override fun isHeader(position: Int): Boolean {
            return position == 0 || (getSectionHeaderName(position) != getSectionHeaderName(position - 1))
        }
        override fun getSectionHeaderName(position: Int): String {
            when(taggedContacts.get(position).second) {
                Contact.KEY_NOT_FOLLOWED_CONTACT -> {return "Friends you might know:"}
                Contact.KEY_FOLLOWED_CONTACT -> {return "Friends you've followed:"}
                else -> {return ""}
            }
        }
    }

    fun getSectionCallback(taggedContacts : ArrayList<Pair<Contact, String>>) : RecyclerItemDecoration.SectionCallback {
        return DecorationSectionCallback(taggedContacts)
    }
}
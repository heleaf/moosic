package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.RecyclerItemDecoration
import com.dev.moosic.adapters.TaggedContactAdapter
import com.dev.moosic.controllers.FriendsController
import com.dev.moosic.models.Contact
import org.parceler.Parcels

private const val ARG_TAGGED_CONTACT_LIST = "taggedContactList"
private const val STR_NOT_FOLLOWED_CONTACT = "Friends you might know:"
private const val STR_FOLLOWED_CONTACT = "Friends you've followed:"
private const val STR_RECOMMENDED_CONTACT = "Recommended Users:"
private const val STR_UNKNOWN_TAG = ""
private const val STR_INVALID_INDEX = "Invalid index"
private const val TAG = "FriendsFragment"

class FriendsFragment(private var friendsController: FriendsController) : Fragment() {
    private lateinit var rvContacts : RecyclerView
    lateinit var adapter : TaggedContactAdapter
    lateinit var emptyFriendsText : TextView
    private var taggedContactList : ArrayList<Pair<Contact, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taggedContactList = Parcels.unwrap(it.getParcelable(ARG_TAGGED_CONTACT_LIST))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(taggedContactList: ArrayList<Pair<Contact, String>>,
            controller: FriendsController) =
            FriendsFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TAGGED_CONTACT_LIST, Parcels.wrap(taggedContactList))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvContacts = view.findViewById(R.id.rvContacts)
        emptyFriendsText = view.findViewById(R.id.emptyFriendsDescription)

        if (taggedContactList.size == 0) {
            emptyFriendsText.visibility = View.VISIBLE
        } else emptyFriendsText.visibility = View.GONE

        adapter = TaggedContactAdapter(view.context, taggedContactList, friendsController)
        rvContacts.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        rvContacts.layoutManager = linearLayoutManager

        val recyclerItemDecoration : RecyclerItemDecoration
        = RecyclerItemDecoration(requireContext(), resources.getDimensionPixelSize(R.dimen.header_height),
        false, getSectionCallback(taggedContactList))
        rvContacts.addItemDecoration(recyclerItemDecoration)
    }

    inner class DecorationSectionCallback(taggedContacts: ArrayList<Pair<Contact, String>>)
        : RecyclerItemDecoration.SectionCallback {
        val taggedContacts: ArrayList<Pair<Contact, String>>
        init {
            this.taggedContacts = taggedContacts
        }
        override fun isHeader(position: Int): Boolean {
            return position == 0 ||
                    (getSectionHeaderName(position)
                            != getSectionHeaderName(position - 1))
        }
        override fun getSectionHeaderName(position: Int): String {
            if (position !in IntRange(0, taggedContacts.size - 1)) return STR_INVALID_INDEX
            return when(taggedContacts.get(position).second) {
                Contact.KEY_NOT_FOLLOWED_CONTACT -> {  STR_NOT_FOLLOWED_CONTACT }
                Contact.KEY_FOLLOWED_CONTACT -> { STR_FOLLOWED_CONTACT }
                Contact.KEY_RECOMMENDED_CONTACT -> { STR_RECOMMENDED_CONTACT }
                else -> { STR_UNKNOWN_TAG }
            }
        }
    }

    private fun getSectionCallback(taggedContacts : ArrayList<Pair<Contact, String>>)
    : RecyclerItemDecoration.SectionCallback {
        return DecorationSectionCallback(taggedContacts)
    }
}
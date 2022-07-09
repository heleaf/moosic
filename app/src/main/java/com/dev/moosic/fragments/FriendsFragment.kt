package com.dev.moosic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.adapters.ContactAdapter
import com.dev.moosic.models.Contact
import org.parceler.Parcels

private const val ARG_PARAM1 = "contactList"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendsFragment : Fragment() {

    private lateinit var rvContacts : RecyclerView
    private lateinit var adapter : ContactAdapter
    private var contactList: ArrayList<Contact> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contactList = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
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
        fun newInstance(contactList : ArrayList<Contact>) =
            FriendsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(contactList))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvContacts = view.findViewById(R.id.rvContacts)
        adapter = ContactAdapter(view.context, contactList)
        rvContacts.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        rvContacts.layoutManager = linearLayoutManager
    }
}
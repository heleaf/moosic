package com.dev.moosic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R

class InterestItemAdapter(context: Context, items: ArrayList<String>,
checkedItems: ArrayList<String>) : RecyclerView.Adapter<InterestItemAdapter.ViewHolder>() {

    var context: Context? = null
    var items: ArrayList<String> = ArrayList()
    var checkedItems: ArrayList<String> = ArrayList()

    init {
        this.context = context
        this.items = items
        this.checkedItems = checkedItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(this.context).inflate(R.layout.single_interest_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = this.items.get(position)
        holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return this.items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var checkListItem: CheckBox? = null
        init {
            checkListItem = itemView.findViewById(R.id.interestItemCheckbox)
        }
        fun bind(item: String, position: Int) {
            checkListItem?.setText(item) // set the text of the checklist
            checkListItem?.isChecked = item in checkedItems
            checkListItem?.setOnClickListener {
                if (item in checkedItems) {
                    checkedItems.remove(item)
                    checkListItem?.isChecked = false
                } else {
                    checkedItems.add(item)
                    checkListItem?.isChecked = true
                }
            }
        }
    }
}
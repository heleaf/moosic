package com.dev.moosic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemDecoration(context: Context, headerHeight: Int,
isSticky: Boolean, callback: SectionCallback) : RecyclerView.ItemDecoration() {

    var context: Context;
    var headerOffset: Int;
    var sticky: Boolean;
    var sectionCallback: SectionCallback;

    var headerView : View? = null
    var title: TextView? = null

    init {
        this.context = context
        this.headerOffset = headerHeight
        this.sticky = isSticky
        this.sectionCallback = callback
    }

    interface SectionCallback {
        fun isHeader(position: Int) : Boolean;
        fun getSectionHeaderName(position: Int) : String;
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val pos = parent.getChildAdapterPosition(view)
        if (sectionCallback.isHeader(pos)){
            outRect.top = headerOffset
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if (headerView == null){
            headerView = inflateHeader(parent)
            title = headerView?.findViewById(R.id.testHeaderTitle)

            fixLayoutSize(headerView!!, parent)
        }
        var prevTitle = ""
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val childPos = parent.getChildAdapterPosition(child)
            val titleText = sectionCallback.getSectionHeaderName(childPos)
            title?.setText(titleText)
            if (!prevTitle.equals(titleText, ignoreCase = true) || sectionCallback.isHeader(childPos)){
                drawHeader(c, child, headerView!!)
                prevTitle = titleText
            }
        }
    }

    private fun drawHeader(c: Canvas, child: View?, headerView: View) {
        c.save()
        if (sticky) {
            if (child != null) {
                c.translate(0F, Math.max(0,child.top - headerView.height).toFloat())
            }
        } else {
            if (child != null) {
                c.translate(0F, (child.top - headerView.height).toFloat())
            };
        }
        headerView.draw(c)
        c.restore()
    }

    fun inflateHeader(recyclerView: RecyclerView): View {
        val view = LayoutInflater.from(this.context).inflate(R.layout.test_header, recyclerView, false)
        return view
    }

    fun fixLayoutSize(headerView: View, parent: ViewGroup): Unit {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        val childWidth = ViewGroup.getChildMeasureSpec(widthSpec, parent.paddingLeft + parent.paddingRight,
        headerView.layoutParams.width)
        val childHeight = ViewGroup.getChildMeasureSpec(heightSpec, parent.paddingTop + parent.paddingBottom,
        headerView.layoutParams.height)

        headerView.measure(childWidth, childHeight);
        headerView.layout(0, 0, headerView.measuredWidth, headerView.measuredHeight)
    }
}
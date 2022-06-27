package com.dev.moosic

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

interface LoadMoreFunction {
    fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?)
}

class EndlessRecyclerViewScrollListener(layoutManager: LinearLayoutManager, loadMoreFunction: LoadMoreFunction) :
    RecyclerView.OnScrollListener() {
    private var visibleThreshold = 5
    private var currentPage = 0
    private var previousTotalItemCount = 0
    private var loading = true

    private val startingPageIndex = 0
    var mLayoutManager: RecyclerView.LayoutManager? = null
    private var mLoadMoreFunction = loadMoreFunction

    init {
        mLayoutManager = layoutManager
    }

//    constructor(layoutManager: GridLayoutManager) {
//        mLayoutManager = layoutManager
//        visibleThreshold = visibleThreshold * layoutManager.spanCount
//    }
//
//    constructor(layoutManager: StaggeredGridLayoutManager) {
//        mLayoutManager = layoutManager
//        visibleThreshold = visibleThreshold * layoutManager.spanCount
//    }

    fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        var lastVisibleItemPosition = 0
        val totalItemCount = mLayoutManager?.itemCount
        if (mLayoutManager is StaggeredGridLayoutManager) {
            val lastVisibleItemPositions =
                (mLayoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
            lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
        } else if (mLayoutManager is GridLayoutManager) {
            lastVisibleItemPosition =
                (mLayoutManager as GridLayoutManager).findLastVisibleItemPosition()
        } else if (mLayoutManager is LinearLayoutManager) {
            lastVisibleItemPosition =
                (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        }

        if (totalItemCount != null) {
            if (totalItemCount < previousTotalItemCount) {
                currentPage = startingPageIndex
                previousTotalItemCount = totalItemCount
                if (totalItemCount == 0) {
                    loading = true
                }
            }

            if (loading && totalItemCount > previousTotalItemCount) {
                loading = false
                previousTotalItemCount = totalItemCount
            }


        if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            currentPage++
            mLoadMoreFunction.onLoadMore(currentPage, totalItemCount, view)
            loading = true
        }

        }
    }

    fun resetState() {
        currentPage = startingPageIndex
        previousTotalItemCount = 0
        loading = true
    }

//    abstract fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?)
}
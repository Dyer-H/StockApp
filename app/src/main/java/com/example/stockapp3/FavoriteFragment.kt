package com.example.stockapp3

import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoriteFragment(private var favoriteListener: FavoriteListener) : Fragment(), TickerAdapter.TickerAdapterListener {
    private lateinit var recycler: RecyclerView
    private lateinit var cursor: Cursor
    private lateinit var decoration: DividerItemDecoration

    interface FavoriteListener{
        fun clickTickerFavorite(ticker: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        recycler = view.findViewById<RecyclerView>(R.id.favoritePage_recyclerView)

        createItemDecoration()
        if (this::cursor.isInitialized){
            createAdapter()
        }
        return view
    }
    fun setCursor(newCursor: Cursor){
        cursor = newCursor
    }

    private fun createAdapter(){
        val tickerAdapter = TickerAdapter(this, cursor)
        recycler.adapter = tickerAdapter
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.addItemDecoration(decoration)
    }

    private fun createItemDecoration(){
        decoration = DividerItemDecoration(
            recycler.context,
            LinearLayoutManager.VERTICAL
        )
    }

    override fun clickTicker(position: Int) {
        cursor.moveToPosition(position)
        val ticker = StockWrapper(cursor).getTicker()
        favoriteListener.clickTickerFavorite(ticker)
    }
}
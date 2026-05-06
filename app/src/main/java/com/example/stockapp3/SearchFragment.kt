package com.example.stockapp3

import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchFragment(private val searchListener: SearchListener) : Fragment(), TickerAdapter.TickerAdapterListener {
    private lateinit var searchBar: SearchView
    private lateinit var recycler: RecyclerView
    private lateinit var generalCursor: Cursor
    private lateinit var searchCursor: MatrixCursor
    private lateinit var tickerAdapter: TickerAdapter
    private lateinit var decoration: DividerItemDecoration

    interface SearchListener{
        fun clickTicker(ticker: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchBar = view.findViewById<SearchView>(R.id.searchPage_searchView)
        recycler = view.findViewById<RecyclerView>(R.id.searchPage_recyclerView)

        createItemDecoration()
        if (this::searchCursor.isInitialized){
            createAdapter()
            setSearchListener()
        }
        return view
    }

    fun setCursor(cursor: Cursor){
        generalCursor = cursor
        createSearchCursor()
    }

    private fun setSearchListener(){
        searchBar.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 == null){
                    tickerAdapter.updateCursor(searchCursor)
                    updateAdapter()
                    return true
                }
                searchCursor.close()
                val columns = arrayOf(StockDatabaseHelper.StockContract.COLUMN_TICKER_SYMBOL)
                searchCursor = MatrixCursor(columns)
                var valid = generalCursor.moveToFirst()
                while(valid){
                    val ticker = StockWrapper(generalCursor).getTicker()
                    if (ticker.lowercase().contains(p0.lowercase())){
                        searchCursor.addRow(arrayOf(ticker))
                    }
                    valid = generalCursor.moveToNext()
                }
                tickerAdapter.updateCursor(searchCursor)
                updateAdapter()
                return true
            }

            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }
        })
    }

    private fun createAdapter(){
        tickerAdapter = TickerAdapter(this, searchCursor)
        recycler.adapter = tickerAdapter
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.addItemDecoration(decoration)
    }

    private fun updateAdapter(){
        recycler.adapter?.notifyDataSetChanged()
    }

    private fun createItemDecoration(){
        decoration = DividerItemDecoration(
            recycler.context,
            LinearLayoutManager.VERTICAL
        )
    }

    private fun createSearchCursor(){
        val columns = arrayOf(StockDatabaseHelper.StockContract.COLUMN_TICKER_SYMBOL)
        searchCursor = MatrixCursor(columns)
        var valid = generalCursor.moveToFirst()
        while(valid){
            val ticker = StockWrapper(generalCursor).getTicker()
            searchCursor.addRow(arrayOf(ticker))
            valid = generalCursor.moveToNext()
        }
    }

    override fun clickTicker(position: Int) {
        searchCursor.moveToPosition(position)
        val ticker = StockWrapper(searchCursor).getTicker()
        searchListener.clickTicker(ticker)
    }
}
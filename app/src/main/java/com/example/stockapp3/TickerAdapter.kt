package com.example.stockapp3

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TickerAdapter(
    private var tickerAdapterListener: TickerAdapterListener,
    private var cursor: Cursor
): RecyclerView.Adapter<TickerAdapter.ViewHolder>() {
    interface TickerAdapterListener {
        fun clickTicker(position: Int)
    }

    class ViewHolder(itemView: View, private val tickerAdapterListener: TickerAdapterListener): RecyclerView.ViewHolder(itemView){
        private val tickerText = itemView.findViewById<TextView>(R.id.tickerRecycler_textView)

        init{
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION){
                    tickerAdapterListener.clickTicker(position)
                }
            }
        }

        fun updateTicker(ticker: String){
            tickerText.text = ticker
        }
    }

    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.ticker_recycler, p0, false)
        return ViewHolder(view, tickerAdapterListener)
    }
    override fun onBindViewHolder(
        p0: ViewHolder,
        p1: Int
    ) {
        cursor.moveToPosition(p1)
        val ticker = StockWrapper(cursor).getTicker()
        p0.updateTicker(ticker)
    }
    override fun getItemCount(): Int {
        return cursor.count
    }
    fun updateCursor(newCursor: Cursor){
        cursor = newCursor
    }
}
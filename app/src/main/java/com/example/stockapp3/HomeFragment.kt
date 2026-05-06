package com.example.stockapp3

import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment(private val favoriteListener: FavoriteListener) : Fragment() {
    private lateinit var title: TextView
    private lateinit var favoriteButton: ImageView
    private lateinit var graphOpen: LineChart
    private lateinit var graphClose: LineChart
    private lateinit var graphHigh: LineChart
    private lateinit var graphLow: LineChart
    private lateinit var graphVolume: LineChart
    private lateinit var stockCursor: Cursor

    private var favorite = false

    interface FavoriteListener{
        fun clickFavorite(favorite: Boolean)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        title = view.findViewById<TextView>(R.id.homePage_textView)
        favoriteButton = view.findViewById<ImageView>(R.id.homePage_imageView)
        graphOpen = view.findViewById<LineChart>(R.id.homePage_lineChartOpen)
        graphClose = view.findViewById<LineChart>(R.id.homePage_lineChartClose)
        graphHigh = view.findViewById<LineChart>(R.id.homePage_lineChartHigh)
        graphLow = view.findViewById<LineChart>(R.id.homePage_lineChartLow)
        graphVolume = view.findViewById<LineChart>(R.id.homePage_lineChartVolume)

        if (this::stockCursor.isInitialized){
            favoriteButton.visibility = View.VISIBLE
            setTitle()
            populateGraphs()
            setFavoriteOnClick()
            toggleFavorite()
        } else {
            favoriteButton.visibility = View.GONE
            setDefaultTitle()
        }
        return view
    }

    fun setCursor(cursor: Cursor){
        stockCursor = cursor
    }
    fun setFavorite(fav: Boolean){
        favorite = !fav
    }
    private fun setDefaultTitle(){
        title.text = getString(R.string.no_selection)
    }
    private fun setTitle(){
        stockCursor.moveToFirst()
        val ticker = StockWrapper(stockCursor).getTicker()
        title.text = ticker
    }
    private fun populateGraphs(){
        viewLifecycleOwner.lifecycleScope.launch{
            val openData = withContext(Dispatchers.IO){
                generateOpenGraph()
            }
            val closeData = withContext(Dispatchers.IO){
                generateCloseGraph()
            }
            val highData = withContext(Dispatchers.IO){
                generateHighGraph()
            }
            val lowData = withContext(Dispatchers.IO){
                generateLowGraph()
            }
            val volumeData = withContext(Dispatchers.IO){
                generateVolumeGraph()
            }
            graphOpen.data = LineData(openData)
            graphOpen.invalidate()
            graphClose.data = LineData(closeData)
            graphClose.invalidate()
            graphHigh.data = LineData(highData)
            graphHigh.invalidate()
            graphLow.data = LineData(lowData)
            graphLow.invalidate()
            graphVolume.data = LineData(volumeData)
            graphVolume.invalidate()
        }
    }

    private fun generateOpenGraph(): LineDataSet{
        var exists = stockCursor.moveToFirst()
        var size = stockCursor.count
        val stockWrapper = StockWrapper(stockCursor)
        val entries = mutableListOf<Entry>()
        while(exists){
            entries.add(Entry(size.toFloat(), stockWrapper.getOpen()))
            exists = stockCursor.moveToNext()
            size -= 1
        }
        entries.sortBy{ it.x }
        val dataset = LineDataSet(entries, "Open Price")
        dataset.setColor(Color.BLUE)
        dataset.setDrawCircles(false)
        dataset.setDrawValues(false)
        return dataset
    }
    private fun generateCloseGraph(): LineDataSet{
        var exists = stockCursor.moveToFirst()
        var size = stockCursor.count
        val stockWrapper = StockWrapper(stockCursor)
        val entries = mutableListOf<Entry>()
        while(exists){
            entries.add(Entry(size.toFloat(), stockWrapper.getClose()))
            exists = stockCursor.moveToNext()
            size -= 1
        }
        entries.sortBy{ it.x }
        val dataset = LineDataSet(entries, "Closing Price")
        dataset.setColor(Color.BLUE)
        dataset.setDrawCircles(false)
        dataset.setDrawValues(false)
        return dataset
    }
    private fun generateHighGraph(): LineDataSet{
        var exists = stockCursor.moveToFirst()
        var size = stockCursor.count
        val stockWrapper = StockWrapper(stockCursor)
        val entries = mutableListOf<Entry>()
        while(exists){
            entries.add(Entry(size.toFloat(), stockWrapper.getHigh()))
            exists = stockCursor.moveToNext()
            size -= 1
        }
        entries.sortBy{ it.x }
        val dataset = LineDataSet(entries, "High Price")
        dataset.setColor(Color.BLUE)
        dataset.setDrawCircles(false)
        dataset.setDrawValues(false)
        return dataset
    }
    private fun generateLowGraph(): LineDataSet{
        var exists = stockCursor.moveToFirst()
        var size = stockCursor.count
        val stockWrapper = StockWrapper(stockCursor)
        val entries = mutableListOf<Entry>()
        while(exists){
            entries.add(Entry(size.toFloat(), stockWrapper.getLow()))
            exists = stockCursor.moveToNext()
            size -= 1
        }
        entries.sortBy{ it.x }
        val dataset = LineDataSet(entries, "Low Price")
        dataset.setColor(Color.BLUE)
        dataset.setDrawCircles(false)
        dataset.setDrawValues(false)
        return dataset
    }
    private fun generateVolumeGraph(): LineDataSet{
        var exists = stockCursor.moveToFirst()
        var size = stockCursor.count
        val stockWrapper = StockWrapper(stockCursor)
        val entries = mutableListOf<Entry>()
        while(exists){
            entries.add(Entry(size.toFloat(), stockWrapper.getVolume().toFloat()))
            exists = stockCursor.moveToNext()
            size -= 1
        }
        entries.sortBy{ it.x }
        val dataset = LineDataSet(entries, "Volume")
        dataset.setColor(Color.BLUE)
        dataset.setDrawCircles(false)
        dataset.setDrawValues(false)
        return dataset
    }

    private fun toggleFavorite(){
        when(favorite){
            true -> {
                favoriteButton.setImageResource(R.drawable.favorite_border)
                favorite = false
            }
            false -> {
                favoriteButton.setImageResource(R.drawable.favorite)
                favorite = true
            }
        }
    }
    private fun setFavoriteOnClick(){
        favoriteButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                toggleFavorite()
                favoriteListener.clickFavorite(favorite)
            }
        })
    }
}
package com.example.stockapp3

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity(), SearchFragment.SearchListener, HomeFragment.FavoriteListener, FavoriteFragment.FavoriteListener{
    private lateinit var navBar: BottomNavigationView
    private lateinit var progressLayout: LinearLayout
    private lateinit var progressText: TextView
    private lateinit var stockDatabaseHelper: StockDatabaseHelper
    private lateinit var tickerCursor: Cursor
    private lateinit var currentCursor: Cursor
    private var favoriteCursor: Cursor? = null

    private var currScreen = ScreenIdentifier.HOME_SCREEN
    private var favCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        progressLayout = findViewById<LinearLayout>(R.id.progressLayout)
        progressText = findViewById<TextView>(R.id.progress_textView)
        navBar = findViewById<BottomNavigationView>(R.id.mainNavigationView)

        updateFragment()
        setBottomNavigationListeners()
        startReload()
    }
    override fun clickTicker(ticker: String) {
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                favCheck = stockDatabaseHelper.checkStock(ticker)
                val isCurrent = stockDatabaseHelper.checkCurrent(ticker)
                if(isCurrent){
                    currScreen = ScreenIdentifier.HOME_SCREEN
                    updateFragment()
                    return@withContext
                }
                if(favCheck){
                    currentCursor = stockDatabaseHelper.getStock(ticker)
                    currScreen = ScreenIdentifier.HOME_SCREEN
                    updateFragment()
                    return@withContext
                }
                val currData = StockAPIHelper.getTickerData(ticker)
                if (currData.isEmpty()){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, R.string.api_fail, Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }
                stockDatabaseHelper.clearCurrent()
                stockDatabaseHelper.insertCurrent(currData)
                currentCursor = stockDatabaseHelper.getCurrent()
                currScreen = ScreenIdentifier.HOME_SCREEN
                updateFragment()
            }
            navBar.selectedItemId = R.id.home_item
        }
    }
    override fun clickFavorite(favorite: Boolean) {
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                currentCursor.moveToFirst()
                val stockWrapper = StockWrapper(currentCursor)
                val ticker = stockWrapper.getTicker()
                stockDatabaseHelper.deleteStock(ticker)
                if (favorite){
                    stockDatabaseHelper.insertStock(currentCursor)
                    favCheck = true
                } else {
                    favCheck = false
                }
                val stockNameCursor = stockDatabaseHelper.getUniqueStocks()
                val valid = stockNameCursor.moveToFirst()
                if (valid){
                    favoriteCursor = stockNameCursor
                } else {
                    favoriteCursor = null
                }
            }
        }
    }
    override fun clickTickerFavorite(ticker: String) {
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                favCheck = true
                val isCurrent = stockDatabaseHelper.checkCurrent(ticker)
                if(isCurrent){
                    currScreen = ScreenIdentifier.HOME_SCREEN
                    updateFragment()
                    return@withContext
                }
                currentCursor = stockDatabaseHelper.getStock(ticker)
                stockDatabaseHelper.clearCurrent()
                stockDatabaseHelper.insertCurrent(currentCursor)
                currScreen = ScreenIdentifier.HOME_SCREEN
                updateFragment()
            }
            navBar.selectedItemId = R.id.home_item
        }
    }

    private fun setBottomNavigationListeners(){
        navBar.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.home_item -> {
                    currScreen = ScreenIdentifier.HOME_SCREEN
                    updateFragment()
                    true
                }
                R.id.search_item -> {
                    currScreen = ScreenIdentifier.SEARCH_SCREEN
                    updateFragment()
                    true
                }
                R.id.list_item -> {
                    currScreen = ScreenIdentifier.LIST_SCREEN
                    updateFragment()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateFragment(){
        when(currScreen){
            ScreenIdentifier.HOME_SCREEN -> {
                val fragment = HomeFragment(this)
                if (this::currentCursor.isInitialized){
                    fragment.setCursor(currentCursor)
                    fragment.setFavorite(favCheck)
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFrameLayout, fragment)
                    .commit()

            }
            ScreenIdentifier.SEARCH_SCREEN -> {
                val fragment = SearchFragment(this)
                if (this::tickerCursor.isInitialized){
                    fragment.setCursor(tickerCursor)
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFrameLayout, fragment)
                    .commit()
            }
            ScreenIdentifier.LIST_SCREEN -> {
                val fragment = FavoriteFragment(this)
                if (favoriteCursor != null){
                    fragment.setCursor(favoriteCursor!!)
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFrameLayout, fragment)
                    .commit()
            }
        }
    }
    private fun startReload(){
       lifecycleScope.launch{
           progressLayout.visibility = View.VISIBLE
           progressText.text = getString(R.string.refresh_all)
           withContext(Dispatchers.IO){
               establishDatabaseConnection()
               val date = getDate()
               updateTickers(date)
               updateCurrent(date)
               updateFavorite(date)
           }
           progressLayout.visibility = View.GONE
       }
    }
    private fun establishDatabaseConnection(){
        stockDatabaseHelper = StockDatabaseHelper(this@MainActivity)
        stockDatabaseHelper.writableDatabase
    }
    private fun getDate(): String{
        val time = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(time)
    }

    private suspend fun updateTickers(date: String){
        val updated = stockDatabaseHelper.checkType(StockDatabaseHelper.StockContract.TYPE_TICKER, date)
        if (updated){
            tickerCursor = stockDatabaseHelper.getTickers()
            if(currScreen == ScreenIdentifier.SEARCH_SCREEN){
                updateFragment()
            }
            return
        }
        stockDatabaseHelper.deleteType(StockDatabaseHelper.StockContract.TYPE_TICKER)
        val tickers = StockAPIHelper.refreshValidTickers(stockDatabaseHelper)
        if (tickers.isEmpty()){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, R.string.refresh_failed, Toast.LENGTH_LONG).show()
            }
            return
        }
        stockDatabaseHelper.clearTickers()
        stockDatabaseHelper.insertTickers(tickers)
        stockDatabaseHelper.addType(StockDatabaseHelper.StockContract.TYPE_TICKER, date)
        tickerCursor = stockDatabaseHelper.getTickers()
        if(currScreen == ScreenIdentifier.SEARCH_SCREEN){
            updateFragment()
        }
    }
    private suspend fun updateCurrent(date: String){
        val updated = stockDatabaseHelper.checkType(StockDatabaseHelper.StockContract.TYPE_CURRENT, date)
        if (updated){
            val tempCurrentCursor = stockDatabaseHelper.getCurrent()
            val valid = tempCurrentCursor.moveToFirst()
            if (valid){
                currentCursor = tempCurrentCursor
                val ticker = StockWrapper(currentCursor).getTicker()
                favCheck = stockDatabaseHelper.checkStock(ticker)
                if(currScreen == ScreenIdentifier.HOME_SCREEN){
                    updateFragment()
                }
            }
            return
        }
        stockDatabaseHelper.deleteType(StockDatabaseHelper.StockContract.TYPE_CURRENT)
        val tempCurrentCursor = stockDatabaseHelper.getCurrent()
        val valid = tempCurrentCursor.moveToFirst()
        if (!valid){
            tempCurrentCursor.close()
            stockDatabaseHelper.addType(StockDatabaseHelper.StockContract.TYPE_CURRENT, date)
            return
        }
        val ticker = StockWrapper(tempCurrentCursor).getTicker()
        tempCurrentCursor.close()
        val currData = StockAPIHelper.getTickerData(ticker)
        if (currData.isEmpty()){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, R.string.api_fail, Toast.LENGTH_SHORT).show()
            }
            return
        }
        stockDatabaseHelper.clearCurrent()
        stockDatabaseHelper.insertCurrent(currData)
        stockDatabaseHelper.addType(StockDatabaseHelper.StockContract.TYPE_CURRENT, date)
        currentCursor = stockDatabaseHelper.getCurrent()
        favCheck = stockDatabaseHelper.checkStock(ticker)
        if(currScreen == ScreenIdentifier.HOME_SCREEN){
            updateFragment()
        }
    }
    private suspend fun updateFavorite(date: String){
        val updated = stockDatabaseHelper.checkType(StockDatabaseHelper.StockContract.TYPE_FAVORITES, date)
        if (updated){
            val stockNameCursor = stockDatabaseHelper.getUniqueStocks()
            val valid = stockNameCursor.moveToFirst()
            if (valid){
                favoriteCursor = stockNameCursor
                if(currScreen == ScreenIdentifier.LIST_SCREEN){
                    updateFragment()
                }
            }
            return
        }
        stockDatabaseHelper.deleteType(StockDatabaseHelper.StockContract.TYPE_FAVORITES)
        val stockNameCursor = stockDatabaseHelper.getUniqueStocks()
        var valid = stockNameCursor.moveToFirst()
        val stockWrapper = StockWrapper(stockNameCursor)
        while(valid){
            val ticker = stockWrapper.getTicker()
            val inCurrent = stockDatabaseHelper.checkCurrent(ticker)
            if (inCurrent){
                stockDatabaseHelper.deleteStock(ticker)
                stockDatabaseHelper.insertStock(currentCursor)
            } else {
                val updatedData = StockAPIHelper.getTickerData(ticker)
                if (updatedData.isEmpty()){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, R.string.api_fail, Toast.LENGTH_SHORT).show()
                    }
                    return
                }
                stockDatabaseHelper.deleteStock(ticker)
                stockDatabaseHelper.insertStock(updatedData)
            }
            valid = stockNameCursor.moveToNext()
        }
        stockDatabaseHelper.addType(StockDatabaseHelper.StockContract.TYPE_FAVORITES, date)
        if (stockNameCursor.moveToFirst()){
            favoriteCursor = stockNameCursor
            if(currScreen == ScreenIdentifier.LIST_SCREEN){
                updateFragment()
            }
        }
        stockNameCursor.close()
    }
}
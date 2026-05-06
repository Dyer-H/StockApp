package com.example.stockapp3

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.random.Random

class StockAPIHelper {
    companion object {
        suspend fun refreshValidTickers(stockDatabaseHelper: StockDatabaseHelper): MutableList<String> {
            val tickers: MutableList<String> = mutableListOf()
            var httpsURLConnection: HttpsURLConnection? = null
            try {
                val url = URL("https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=demo")
                httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.requestMethod = "GET"

                if (httpsURLConnection.responseCode == HttpsURLConnection.HTTP_OK) {
                    val inputStream = httpsURLConnection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    bufferedReader.use{ br ->
                        br.readLine()
                        var line: String? = br.readLine()
                        while (line != null){
                            val data = line.split(",")
                            tickers.add(data[0])
                            line = br.readLine()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                httpsURLConnection?.disconnect()
            }
            return tickers
        }
        suspend fun getTickerData(ticker: String): MutableList<Stock>{
            val stocks: MutableList<Stock> = mutableListOf()
            var result = ""
            var httpsURLConnection: HttpsURLConnection? = null
            try {
                val url = URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=$ticker&apikey=YOUR_API_KEY")
                httpsURLConnection = url.openConnection() as HttpsURLConnection
                httpsURLConnection.requestMethod = "GET"

                if (httpsURLConnection.responseCode == HttpsURLConnection.HTTP_OK) {
                    val inputStream = httpsURLConnection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    result = bufferedReader.readText()
                    bufferedReader.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                httpsURLConnection?.disconnect()
            }
            try {
                val jsonObject = JSONObject(result)
                val timeSeries = jsonObject.getJSONObject("Time Series (Daily)")
                val dates = timeSeries.keys()
                while (dates.hasNext()){
                    val date = dates.next()
                    val stockData = timeSeries.getJSONObject(date)
                    val open = stockData.getString("1. open")
                    val high = stockData.getString("2. high")
                    val low = stockData.getString("3. low")
                    val close = stockData.getString("4. close")
                    val volume = stockData.getString("5. volume")
                    val stock = Stock(
                        ticker,
                        date,
                        open.toFloat(),
                        high.toFloat(),
                        low.toFloat(),
                        close.toFloat(),
                        volume.toInt()
                    )
                    stocks.add(stock)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return stocks
        }
    }
}
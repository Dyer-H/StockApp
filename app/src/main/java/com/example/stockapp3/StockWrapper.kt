package com.example.stockapp3

import android.database.Cursor

class StockWrapper(private val cursor: Cursor) {
    fun getTicker(): String{
        return cursor.getString(cursor.getColumnIndexOrThrow(StockDatabaseHelper.StockContract.COLUMN_TICKER_SYMBOL))
    }
    fun getDate(): String{
        return cursor.getString(cursor.getColumnIndexOrThrow(StockDatabaseHelper.StockContract.COLUMN_DATE))
    }
    fun getOpen(): Float{
        return cursor.getFloat(cursor.getColumnIndexOrThrow(StockDatabaseHelper.StockContract.COLUMN_OPEN))
    }
    fun getClose(): Float{
        return cursor.getFloat(cursor.getColumnIndexOrThrow(StockDatabaseHelper.StockContract.COLUMN_CLOSE))
    }
    fun getHigh(): Float{
        return cursor.getFloat(cursor.getColumnIndexOrThrow(StockDatabaseHelper.StockContract.COLUMN_HIGH))
    }
    fun getLow(): Float{
        return cursor.getFloat(cursor.getColumnIndexOrThrow(StockDatabaseHelper.StockContract.COLUMN_LOW))
    }
    fun getVolume(): Int{
        return cursor.getInt(cursor.getColumnIndexOrThrow(StockDatabaseHelper.StockContract.COLUMN_VOLUME))
    }
    fun getStock(): Stock{
        return Stock(
            getTicker(),
            getDate(),
            getOpen(),
            getClose(),
            getHigh(),
            getLow(),
            getVolume()
        )
    }
}
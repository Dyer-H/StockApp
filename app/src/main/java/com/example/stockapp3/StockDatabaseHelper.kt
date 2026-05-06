package com.example.stockapp3

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.SQLException

class StockDatabaseHelper(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){
    object StockContract{
        const val TABLE_NAME_TICKER = "ticker"
        const val TABLE_NAME_STOCK = "stock"
        const val TABLE_NAME_CURRENT = "current"
        const val TABLE_NAME_LAST = "last"
        const val COLUMN_ID = "_id"
        const val COLUMN_TICKER_SYMBOL = "ticker"
        const val COLUMN_DATE = "date"
        const val COLUMN_OPEN = "open"
        const val COLUMN_CLOSE = "close"
        const val COLUMN_HIGH = "high"
        const val COLUMN_LOW = "low"
        const val COLUMN_VOLUME = "volume"
        const val COLUMN_TYPE = "type"

        const val TYPE_TICKER = "tickers"
        const val TYPE_FAVORITES = "favorites"
        const val TYPE_CURRENT = "current"

        const val CREATE_TABLE_TICKER = """
            CREATE TABLE $TABLE_NAME_TICKER(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TICKER_SYMBOL TEXT
            );
        """
        const val CREATE_TABLE_STOCK = """
            CREATE TABLE $TABLE_NAME_STOCK(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE TEXT,
                $COLUMN_OPEN REAL,
                $COLUMN_CLOSE REAL,
                $COLUMN_HIGH REAL,
                $COLUMN_LOW REAL,
                $COLUMN_VOLUME INTEGER, 
                $COLUMN_TICKER_SYMBOL TEXT
            );
        """
        const val CREATE_TABLE_CURRENT = """
            CREATE TABLE $TABLE_NAME_CURRENT(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE TEXT,
                $COLUMN_OPEN REAL,
                $COLUMN_CLOSE REAL,
                $COLUMN_HIGH REAL,
                $COLUMN_LOW REAL,
                $COLUMN_VOLUME INTEGER, 
                $COLUMN_TICKER_SYMBOL TEXT
            );
        """
        const val CREATE_TABLE_LAST = """
            CREATE TABLE $TABLE_NAME_LAST(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TYPE TEXT,
                $COLUMN_DATE TEXT
            );
        """
    }
    companion object {
        const val DB_NAME = "stock.db"
        const val DB_VERSION = 1
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL(StockContract.CREATE_TABLE_TICKER)
        p0?.execSQL(StockContract.CREATE_TABLE_STOCK)
        p0?.execSQL(StockContract.CREATE_TABLE_CURRENT)
        p0?.execSQL(StockContract.CREATE_TABLE_LAST)
    }

    override fun onUpgrade(
        p0: SQLiteDatabase?,
        p1: Int,
        p2: Int
    ) {
        TODO("Not yet implemented")
    }

    suspend fun clearTickers(){
        val db = writableDatabase
        val sqlStatement1 = """
            DELETE FROM ${StockContract.TABLE_NAME_TICKER};
        """.trimIndent()
        val sqlStatement2 = """
            UPDATE sqlite_sequence SET seq = 0 WHERE name = '${StockContract.TABLE_NAME_TICKER}';
        """.trimIndent()
        db.execSQL(sqlStatement1)
        db.execSQL(sqlStatement2)
    }
    suspend fun insertTickers(tickers: MutableList<String>){
        val db = writableDatabase
        //https://stackoverflow.com/questions/3860008/bulk-insertion-on-android-device
        try {
            db.beginTransaction()
            for (ticker in tickers){
                val data = ContentValues()
                data.put(StockContract.COLUMN_TICKER_SYMBOL, ticker)
                db.insert(StockContract.TABLE_NAME_TICKER, null, data)
            }
            db.setTransactionSuccessful()
        }
        catch (e: SQLException) {
            e.printStackTrace()
        }
        finally {
            db.endTransaction()
        }
    }
    suspend fun getTickers(): Cursor{
        val db = writableDatabase
        val cursor = db.query(
            StockContract.TABLE_NAME_TICKER,
            arrayOf(
                StockContract.COLUMN_TICKER_SYMBOL
            ),
            null,
            null,
            null,
            null,
            null,
            null
        )
        return cursor
    }

    suspend fun clearCurrent(){
        val db = writableDatabase
        val sqlStatement1 = """
            DELETE FROM ${StockContract.TABLE_NAME_CURRENT};
        """.trimIndent()
        val sqlStatement2 = """
            UPDATE sqlite_sequence SET seq = 0 WHERE name = '${StockContract.TABLE_NAME_CURRENT}';
        """.trimIndent()
        db.execSQL(sqlStatement1)
        db.execSQL(sqlStatement2)
    }
    suspend fun insertCurrent(stockData: MutableList<Stock>){
        val db = writableDatabase
        //https://stackoverflow.com/questions/3860008/bulk-insertion-on-android-device
        try {
            db.beginTransaction()
            for (stock in stockData){
                val data = ContentValues()
                data.put(StockContract.COLUMN_DATE, stock.date)
                data.put(StockContract.COLUMN_OPEN, stock.open)
                data.put(StockContract.COLUMN_HIGH, stock.high)
                data.put(StockContract.COLUMN_LOW, stock.low)
                data.put(StockContract.COLUMN_CLOSE, stock.close)
                data.put(StockContract.COLUMN_VOLUME, stock.volume)
                data.put(StockContract.COLUMN_TICKER_SYMBOL, stock.name)
                db.insert(StockContract.TABLE_NAME_CURRENT, null, data)
            }
            db.setTransactionSuccessful()
        }
        catch (e: SQLException) {
            e.printStackTrace()
        }
        finally {
            db.endTransaction()
        }
    }
    suspend fun insertCurrent(cursor: Cursor){
        val db = writableDatabase

        val stockWrapper = StockWrapper(cursor)
        val stockList = mutableListOf<Stock>()
        var valid = cursor.moveToFirst()
        while(valid){
            stockList.add(stockWrapper.getStock())
            valid = cursor.moveToNext()
        }

        try {
            db.beginTransaction()
            for (stock in stockList){
                val data = ContentValues()
                data.put(StockContract.COLUMN_DATE, stock.date)
                data.put(StockContract.COLUMN_OPEN, stock.open)
                data.put(StockContract.COLUMN_HIGH, stock.high)
                data.put(StockContract.COLUMN_LOW, stock.low)
                data.put(StockContract.COLUMN_CLOSE, stock.close)
                data.put(StockContract.COLUMN_VOLUME, stock.volume)
                data.put(StockContract.COLUMN_TICKER_SYMBOL, stock.name)
                db.insert(StockContract.TABLE_NAME_CURRENT, null, data)
            }
            db.setTransactionSuccessful()
        }
        catch (e: SQLException) {
            e.printStackTrace()
        }
        finally {
            db.endTransaction()
        }
    }
    suspend fun getCurrent(): Cursor{
        val db = writableDatabase
        val cursor = db.query(
            StockContract.TABLE_NAME_CURRENT,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        return cursor
    }
    suspend fun checkCurrent(ticker: String): Boolean{
        val db = writableDatabase
        val cursor = db.query(
            StockContract.TABLE_NAME_CURRENT,
            null,
            "${StockContract.COLUMN_TICKER_SYMBOL} = ?",
            arrayOf(ticker),
            null,
            null,
            null,
            "1"
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    suspend fun checkStock(ticker: String): Boolean{
        val db = writableDatabase
        val cursor = db.query(
            StockContract.TABLE_NAME_STOCK,
            null,
            "${StockContract.COLUMN_TICKER_SYMBOL} = ?",
            arrayOf(ticker),
            null,
            null,
            null,
            "1"
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }
    suspend fun insertStock(stockData: MutableList<Stock>){
        val db = writableDatabase
        try {
            db.beginTransaction()
            for (stock in stockData){
                val data = ContentValues()
                data.put(StockContract.COLUMN_DATE, stock.date)
                data.put(StockContract.COLUMN_OPEN, stock.open)
                data.put(StockContract.COLUMN_HIGH, stock.high)
                data.put(StockContract.COLUMN_LOW, stock.low)
                data.put(StockContract.COLUMN_CLOSE, stock.close)
                data.put(StockContract.COLUMN_VOLUME, stock.volume)
                data.put(StockContract.COLUMN_TICKER_SYMBOL, stock.name)
                db.insert(StockContract.TABLE_NAME_STOCK, null, data)
            }
            db.setTransactionSuccessful()
        }
        catch (e: SQLException) {
            e.printStackTrace()
        }
        finally {
            db.endTransaction()
        }
    }
    suspend fun insertStock(cursor: Cursor){
        val db = writableDatabase

        val stockWrapper = StockWrapper(cursor)
        val stockList = mutableListOf<Stock>()
        var valid = cursor.moveToFirst()
        while(valid){
            stockList.add(stockWrapper.getStock())
            valid = cursor.moveToNext()
        }

        try {
            db.beginTransaction()
            for (stock in stockList){
                val data = ContentValues()
                data.put(StockContract.COLUMN_DATE, stock.date)
                data.put(StockContract.COLUMN_OPEN, stock.open)
                data.put(StockContract.COLUMN_HIGH, stock.high)
                data.put(StockContract.COLUMN_LOW, stock.low)
                data.put(StockContract.COLUMN_CLOSE, stock.close)
                data.put(StockContract.COLUMN_VOLUME, stock.volume)
                data.put(StockContract.COLUMN_TICKER_SYMBOL, stock.name)
                db.insert(StockContract.TABLE_NAME_STOCK, null, data)
            }
            db.setTransactionSuccessful()
        }
        catch (e: SQLException) {
            e.printStackTrace()
        }
        finally {
            db.endTransaction()
        }
    }
    suspend fun deleteStock(ticker: String){
        val db = writableDatabase
        val sqlStatement = """
            DELETE FROM ${StockContract.TABLE_NAME_STOCK} 
            WHERE ${StockContract.COLUMN_TICKER_SYMBOL} = '$ticker';
        """.trimIndent()
        db.execSQL(sqlStatement)
    }
    suspend fun getStock(ticker: String): Cursor{
        val db = writableDatabase
        val cursor = db.query(
            StockContract.TABLE_NAME_STOCK,
            null,
            "${StockContract.COLUMN_TICKER_SYMBOL} = ?",
            arrayOf(ticker),
            null,
            null,
            null,
            null
        )
        return cursor
    }
    suspend fun getUniqueStocks(): Cursor{
        val db = writableDatabase
        val cursor = db.query(
            true,
            StockContract.TABLE_NAME_STOCK,
            arrayOf(StockContract.COLUMN_TICKER_SYMBOL),
            null,
            null,
            null,
            null,
            null,
            null
        )
        return cursor
    }

    suspend fun addType(type: String, date: String){
        val db = writableDatabase
        val value = ContentValues().apply{
            put(StockContract.COLUMN_TYPE, type)
            put(StockContract.COLUMN_DATE, date)
        }
        db.insert(StockContract.TABLE_NAME_LAST, null, value)
    }
    suspend fun checkType(type: String, date: String): Boolean{
        val db = writableDatabase
        val cursor = db.query(
            StockContract.TABLE_NAME_LAST,
            null,
            "${StockContract.COLUMN_TYPE} = ? AND ${StockContract.COLUMN_DATE} = ?",
            arrayOf(type, date),
            null,
            null,
            null,
            "1"
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }
    suspend fun deleteType(type: String){
        val db = writableDatabase
        val sqlStatement = """
            DELETE FROM ${StockContract.TABLE_NAME_LAST} 
            WHERE ${StockContract.COLUMN_TYPE} = '$type';
        """.trimIndent()
        db.execSQL(sqlStatement)
    }
}
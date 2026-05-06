package com.example.stockapp3

class Stock(
    var name: String,
    var date: String,
    var open: Float,
    var high: Float,
    var low: Float,
    var close: Float,
    var volume: Int
){
    //For debugging
    override fun toString(): String{
        return "$name, $volume"
    }
}
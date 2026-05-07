# StockApp
This is an app to view stock data easily on Android.

## Usage
This program queries from Alpha Vantage's API, so you need an API key for it to run properly. 

Once you have your API Key from Alpha Vantage's website, navigate to app/src/main/java/com/example/stockapp3/StockAPIHelper.kt. On line 48, replace YOUR_API_KEY with your api key. 

This project was built and tested in Android Studio, so to run this project, import the files into Android Studio, let it handle the building process, and run it on an emulator of your choice.

## Demo
https://github.com/user-attachments/assets/d34d7555-e509-48c4-bea1-248d52ccbbf4

Apologies for the length of the demo, Android's Emulator can be slow to load sometimes.

## Program Flow
To minimize API calls, items that have been favorited, or that exist currently on the homepage, are stored locally in a database. This way, when we want information for a stock, if it is contained in these databases, we do not have to perform an API call for the information. This information gets updated automatically by the program through the API at every new day.

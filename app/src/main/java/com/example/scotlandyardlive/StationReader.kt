package com.example.scotlandyardlive

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.NumberFormatException

class StationReader(private val context: Context) {

    public
    fun readStationsFromAssets(): Map<Pair<Double,Double>,String> {
        val data = readStationsCSV()

        // read header
        val header = data[0]
        var name = 0
        var x = 0
        var y = 0
        for (i in header.indices) {
            if (header[i] == "HST_NAME") {
                name = i
            }
            if (header[i] == "X_WGS84") {
                x = i
            }
            if (header[i] == "Y_WGS84") {
                y = i
            }
        }

        val stationmap = mutableMapOf<Pair<Double,Double>,String>()


        data.forEachIndexed { index, entry ->
            if (index > 0) {
                // Skip the first entry and perform your desired operations on the remaining entries
                try {
                    val x_coord= entry[x].replace(",",".").toDouble()
                    val y_coord= entry[y].replace(",",".").toDouble()

                    stationmap[Pair(x_coord,y_coord)] = entry[name]

                }catch (e:NumberFormatException){
                    Log.e("StationReader","Error Reading Koordinate value: ${e.message}")
                }
            }
        }

        return stationmap.toMap()
    }

    private
    fun readStationsCSV(): List<Array<String>> {
        val data = mutableListOf<Array<String>>()

        try {
            val inputStream = context.assets.open("RMV_Haltestellen.csv")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))

            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                val row =
                    line!!.split(",") // Change the delimiter if your CSV file uses a different separator
                        .toTypedArray()
                data.add(row)
            }

            bufferedReader.close()
        } catch (e: IOException) {
            Log.e("StationReader", "Error reading CSV file: ${e.message}")
        }

        return data
    }
}
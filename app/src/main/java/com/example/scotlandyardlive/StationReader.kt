package com.example.scotlandyardlive


import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.opencsv.CSVParserBuilder

import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.NumberFormatException

class StationReader(private val context: Context) {

    public
    fun readStationsFromAssets(): Map<Pair<Double, Double>, String> {
        val data = readStationsCSV()

        // read header
        val header = data[0]
        var name = 0
        var x = 0
        var y = 0
        for (i in header.indices) {
            Log.i("StationReader", "${header[i]}")
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

        val stationmap = mutableMapOf<Pair<Double, Double>, String>()


        data.forEachIndexed { index, entry ->
            if (index > 0) {
                // Skip the first entry and perform your desired operations on the remaining entries
                try {
                    val x_coord = entry[x].replace(",", ".").toDouble()
                    val y_coord = entry[y].replace(",", ".").toDouble()

                    stationmap[Pair(x_coord, y_coord)] = entry[name]

                } catch (e: NumberFormatException) {
                    Log.e("StationReader", "Error Reading Coordinate value: ${e.message}")
                }
            }
        }

        return stationmap.toMap()
    }

    private
    fun readStationsCSV(): List<Array<String>> {


        try {
            val assetManager: AssetManager = context.assets
            val inputStream = assetManager.open("RMV_Haltestellen.csv")
            val reader = CSVReaderBuilder(InputStreamReader(inputStream))
                .withCSVParser(
                     CSVParserBuilder()
                        .withSeparator(';')
                        .build()
                ).build()

            val data: MutableList<Array<String>> = mutableListOf()

            var line: Array<String>?
            while (reader.readNext().also { line = it } != null) {
                line?.let {
                    val nonNullLine = it.map { field -> field ?: "" }.toTypedArray()
                    data.add(nonNullLine)
                }
            }
            reader.close()
            inputStream.close()
            return data.toList()

        } catch (e: IOException) {
            Log.e("StationReader", "Error reading CSV file: ${e.message}")
        }
        return emptyList()
    }
}
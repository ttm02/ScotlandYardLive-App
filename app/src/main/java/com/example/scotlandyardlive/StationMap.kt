package com.example.scotlandyardlive

import android.app.AlertDialog
import android.content.Context

import dmax.dialog.SpotsDialog
import java.lang.Thread.sleep
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sqrt


class StationMap private constructor(
    private val map: Map<Pair<Double, Double>, String>,
    private val list: Array<String>
) {

    // Singelton pattern getinstance
    companion object {
        @Volatile
        private var instance: StationMap? = null

        fun getInstance(context: Context): StationMap {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val dialog: AlertDialog = SpotsDialog.Builder().setContext(context)
                            .setMessage("Loading Station List")
                            .setCancelable(false).build()

                        dialog.show()

                        val reader = StationReader(context = context)
                        val data = reader.readStationsFromAssets()

                        instance = StationMap(data,data.mapNotNull { (_, string) -> string }.toTypedArray())

                        dialog.dismiss()
                    }
                }
            }
            return instance!!

        }
    }


    // initialized in constructor
    // TODO: use more efficient datastructure to find the closest station

    fun get_nearest_stations(pos: Pair<Double, Double>, n: Int): List<Pair<String,Int>> {

        assert(n > 0)
        assert(n < map.size)


        var current_best: MutableList<Triple<Pair<Double, Double>, Double, String>> =
            mutableListOf()


        map.forEach { (key, value) ->
            // Perform operation for each key-value pair
            val dist = distance(pos, key)

            var i = 0
            var insert: Boolean = true

            while (i < current_best.size && i < n) {

                if (current_best[i].second > dist && insert) {
                    current_best.add(i, Triple(key, dist, value))
                    insert = false
                    break
                }
                i++
            }
            if (i < n && insert) {
                current_best.add(i, Triple(key, dist, value))
            }

        }

        return current_best.mapNotNull { (_, dist, string) -> Pair(string,dist.roundToInt()) }
    }

    fun distance(a: Pair<Double, Double>, b: Pair<Double, Double>): Double {

        val y_diff = (a.second - b.second) / 111320
        val x_diff = (a.first - b.first) / 40075000 * cos(a.second) / 360
        return sqrt(y_diff * y_diff + x_diff * x_diff)

    }

    fun get_station_list():Array<String>{
        return list
    }

}
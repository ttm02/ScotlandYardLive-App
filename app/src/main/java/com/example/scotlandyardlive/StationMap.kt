package com.example.scotlandyardlive

import android.app.AlertDialog
import android.content.Context
import dmax.dialog.SpotsDialog
import org.osmdroid.util.GeoPoint
import kotlin.math.roundToInt


class StationMap private constructor(
    private val coordinates_to_stations: Map<Pair<Double, Double>, String>,
    private val stations_to_coordinates: Map<String,Pair<Double, Double>>,
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
                        val valueToCoordinates: Map<String, Pair<Double, Double>> = data.entries.associate { (key, value) -> value to key }

                        instance = StationMap(data,valueToCoordinates,data.mapNotNull { (_, string) -> string }.toTypedArray())

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
        assert(n < coordinates_to_stations.size)


        var current_best: MutableList<Triple<Pair<Double, Double>, Double, String>> =
            mutableListOf()


        coordinates_to_stations.forEach { (key, value) ->
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

        // Debugging
        //Log.d("get_nearest_stations","Position: ${pos}")
        //Log.d("get_nearest_stations","${current_best[0]}")

        return current_best.mapNotNull { (_, dist, string) -> Pair(string,dist.roundToInt()) }
    }


    private fun toRad(value: Double): Double {
        return value * Math.PI / 180
    }
    //HaversineDistance
    fun distance(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double {
        val R = 6371000; // Radious of the earth in meters


        //val y_diff = (a.second - b.second) / 111320
        //val x_diff = (a.first - b.first) / 40075000 * cos(a.second) / 360
        //return sqrt(y_diff * y_diff + x_diff * x_diff)
        val lat1 = p1.second
        val lat2 = p2.second
        val lon1 = p1.first
        val lon2 = p2.first

        val latDistance = toRad(lat2 - lat1)
        val lonDistance = toRad(lon2 - lon1)
        val a =
            Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(toRad(lat1)) * Math.cos(
                toRad(lat2)
            ) *
                    Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = R * c
        return distance

    }

    fun get_station_list():Array<String>{
        return list
    }

    fun get_station_position(station_name:String):Pair<Double,Double>?{
        return stations_to_coordinates.get(station_name)
        // or null if not found
    }

}
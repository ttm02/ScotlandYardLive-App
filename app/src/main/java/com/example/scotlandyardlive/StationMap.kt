package com.example.scotlandyardlive

import android.app.AlertDialog
import android.content.Context

import dmax.dialog.SpotsDialog
import java.lang.Thread.sleep


class StationMap private constructor(
    private val map: Map<Pair<Double, Double>, String>
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

                        instance = StationMap(data)

                        dialog.dismiss()
                             }
                }
            }
            return instance!!

        }
    }


    // initialized in constructor
    // TODO: Init function where a tree is built

}
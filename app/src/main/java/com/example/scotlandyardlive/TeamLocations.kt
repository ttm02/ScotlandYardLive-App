package com.example.scotlandyardlive

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.reflect.TypeToken
import dmax.dialog.SpotsDialog
import org.apache.commons.lang3.mutable.Mutable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sqrt
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import java.io.IOException
import java.lang.reflect.Type


data class Team(
    val Name: String,
    val Positions: MutableList<Position>
)

data class Position(
    @JsonAdapter(LocalTimeDeserializer::class)
    val Time: LocalTime,
    val Station: String,
    val Transport: String
)

// String to LocalTime object
class LocalTimeDeserializer : JsonDeserializer<LocalTime> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalTime {
        val timeString = json?.asString
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        return LocalTime.parse(timeString, timeFormatter)
    }
}


// Function to read JSON from assets folder
fun loadJSONFromAsset(context: Context, fileName: String): String? {
    var json: String? = null
    try {
        val inputStream = context.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        json = String(buffer, Charsets.UTF_8)
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
    return json
}

class TeamLocations private constructor(
    private val teamlist: Array<Team>
) {

    // Singelton pattern getinstance
    companion object {
        @Volatile
        private var instance: TeamLocations? = null

        fun getInstance(context: Context): TeamLocations {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {

                        val reader = StationReader(context = context)
                        val data = reader.readStationsFromAssets()

                        val gson = GsonBuilder()
                            .registerTypeAdapter(LocalTime::class.java, LocalTimeDeserializer())
                            .create()

                        val jsonString = loadJSONFromAsset(context, "sample_position.json")
                        val teams: Map<String, Team> = gson.fromJson(
                            jsonString,
                            object : TypeToken<Map<String, Team>>() {}.type
                        )

                        instance = TeamLocations(teams.values.toTypedArray())

                    }
                }
            }
            return instance!!

        }
    }

    fun getteams(): Array<Team> {
        return teamlist
    }


}
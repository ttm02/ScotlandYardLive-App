package com.example.scotlandyardlive

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.BlobId
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.reflect.TypeToken
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import java.io.IOException
import java.lang.reflect.Type

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


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
        try {
            return LocalTime.parse(timeString, timeFormatter)
        } catch (e: Exception) {
            // Handle parsing exception, e.g., log an error or provide a default value
            Log.w("Timedesirialize", e.message!!)
        }
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

// Function to download the JSON file from Google Cloud Storage
fun enqueue_downloadJsonFromCloudStorage(apiKey: String,fileName: String, downloadCallback: DownloadCallback) {

    //val  ByteArrayInputStream stream = ByteArrayInputStream(credential.getBytes(StandardCharsets.UTF_8));

    val inputStream: InputStream = ByteArrayInputStream(apiKey.toByteArray())
    val credentials = GoogleCredentials.fromStream(inputStream)
    val storage: Storage = StorageOptions.newBuilder()
        .setProjectId("scotlandyardliveapp")
        .setCredentials(credentials)
        .build().service

    // Specify the bucket and file name of the JSON file in Cloud Storage
    val bucketName = "scotlandyardlivejson"

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val blobId = BlobId.of(bucketName, fileName)
            val blob = storage.get(blobId)

            if (blob != null) {
                val content: ByteArray = blob.getContent()
                val text: String = content.toString(StandardCharsets.UTF_8)
                downloadCallback.onDownloadComplete(text)
            } else {
                downloadCallback.onDownloadError(Exception("File not found"))
            }
        } catch (e: Exception) {
            downloadCallback.onDownloadError(e)
        }
    }

}

interface DownloadCallback {
    fun onDownloadComplete(result: String)
    fun onDownloadError(error: Exception)
}

class TeamPositionsManager private constructor(
    private val teamlist: Array<Team>,
    private val download_api_key: String,
    // wen requesting an update, we use this counter to count the number of updates received
    private var team_update_counter: AtomicInteger = AtomicInteger(0) ,
    private var team_update_in_progress: AtomicBoolean = AtomicBoolean(false) ,
    private val num_teams: Int = 6
) : LiveData<LocalTime>(), DownloadCallback {

    // Singelton pattern: getinstance
    companion object {
        @Volatile
        private var instance: TeamPositionsManager? = null

        @MainThread
        fun getInstance(context: Context): TeamPositionsManager {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {

                        val gson = GsonBuilder()
                            .registerTypeAdapter(LocalTime::class.java, LocalTimeDeserializer())
                            .create()

                        val jsonString = loadJSONFromAsset(context, "empty_positions.json")
                        val teams: Map<String, Team> = gson.fromJson(
                            jsonString,
                            object : TypeToken<Map<String, Team>>() {}.type
                        )

                        val api_key_json = loadJSONFromAsset(context, "api_key.json")
                        instance = TeamPositionsManager(
                            teams.values.toTypedArray(),
                            api_key_json!!
                        )

                    }
                }
            }
            return instance!!
        }
    }


    fun getteams(): Array<Team> {
        return teamlist
    }

    fun request_updates() {
        //TODO
        if (team_update_in_progress.compareAndSet(false, true)){
            // if no update in progress:
            for (t in teamlist){
                enqueue_downloadJsonFromCloudStorage(download_api_key,t.Name+".json",this)
            }
        }
    }

    private fun finish_update() {
        val assertion =  team_update_counter.compareAndSet(num_teams,0)
        assert (assertion)

        super.postValue(LocalTime.now())
    }

    override fun onDownloadComplete(result: String) {
        // Handle the downloaded string here

        // TODO should we save the gson reader instead of rebuilding it?
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalTime::class.java, LocalTimeDeserializer())
            .create()

        val teams: Map<String, Team> = gson.fromJson(
            result,
            object : TypeToken<Map<String, Team>>() {}.type
        )

        val team = teams.entries.first().value

        var team_found=false
        for (i in teamlist.indices){
            if (team.Name == teamlist[i].Name){
                assert(!team_found)
                team_found=true
                teamlist[i]=team
                //break
                // one could break but our assertion checks that we found only one match
            }
        }
        assert(team_found)

        val count = team_update_counter.incrementAndGet()
        if (count == num_teams){
            finish_update()}
    }

    override fun onDownloadError(error: Exception) {
        // Handle the download error here

        Log.e("TeamManager", "Json Update Failed")
        if (error.message != null) {
            Log.e("TeamManager", error.message!!)
        }

        val count = team_update_counter.incrementAndGet()
        if (count == num_teams){
            finish_update()}
    }

}
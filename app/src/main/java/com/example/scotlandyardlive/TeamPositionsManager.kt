package com.example.scotlandyardlive

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
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
import com.google.gson.InstanceCreator
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.nio.charset.StandardCharsets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread


data class Team(
    val Name: String,
    val Positions: MutableList<Position>,
) {
    fun tojson(): String {
        // TODO should we save the gson reader instead of rebuilding it?
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalTime::class.java, LocalTimeSerializer())
            .registerTypeAdapter(Team::class.java, TeamInstanceCreator())
            .create()

        return "{\"Team\":" + gson.toJson(this) + "}"
    }

    // creates an empty instance for usage with gson
    companion object {
        fun createInstance(): Team {
            // Create a new instance of Team using custom logic
            // For example, you can provide default values or perform additional initialization
            return Team("", mutableListOf())
        }
    }
}

class TeamInstanceCreator : InstanceCreator<Team> {
    override fun createInstance(type: Type): Team {
        return Team.createInstance()
    }
}

data class Position(
    @JsonAdapter(LocalTimeSerializer::class)
    val Time: LocalTime,
    val Station: String,
    val Transport: String
)

//TODO we actually dont need to serialize all the time we could just keep the strings around

class LocalTimeSerializer : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    // LocalTime object to String
    override fun serialize(
        localTime: LocalTime,
        type: Type,
        jsonSerializationContext: JsonSerializationContext
    ): JsonElement {
        val formattedTime = localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        return JsonPrimitive(formattedTime)
    }

    // String to LocalTime object
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
fun enqueue_downloadJsonFromCloudStorage(
    apiKey: String,
    fileName: String,
    downloadCallback: DownloadCallback
) {

    //val  ByteArrayInputStream stream = ByteArrayInputStream(credential.getBytes(StandardCharsets.UTF_8));

    val inputStream: InputStream = ByteArrayInputStream(apiKey.toByteArray())
    val credentials = GoogleCredentials.fromStream(inputStream)
    val storage: Storage = StorageOptions.newBuilder()
        .setProjectId("scotlandyardlive-388807")
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

fun enqueue_UploadJsonToCloudStorage(
    apiKey: String,
    fileName: String,
    json: String,
    uploadCallback: UploadCallback
) {

    //val  ByteArrayInputStream stream = ByteArrayInputStream(credential.getBytes(StandardCharsets.UTF_8));

    val inputStream: InputStream = ByteArrayInputStream(apiKey.toByteArray())
    val credentials = GoogleCredentials.fromStream(inputStream)
    val storage: Storage = StorageOptions.newBuilder()
        .setProjectId("scotlandyardliveapp")
        .setCredentials(credentials)
        .build().service

    val bucketName = "scotlandyardlivejson"

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val blobId = BlobId.of(bucketName, fileName)
            val blobInfo = BlobInfo.newBuilder(blobId).build()

            //TODO we could do some sotz of check if there was a conflicting update

            val jsonStringBytes = json.toByteArray(Charsets.UTF_8)
            storage.create(blobInfo, jsonStringBytes)


            uploadCallback.onUploadComplete()

        } catch (e: Exception) {
            uploadCallback.onUploadError(e)
        }
    }
}

interface DownloadCallback {
    fun onDownloadComplete(result: String)
    fun onDownloadError(error: Exception)
}

interface UploadCallback {
    fun onUploadComplete()
    fun onUploadError(error: Exception)
}

class TeamPositionsManager private constructor(
    private val currentTeam: String,
    private val teamlist: Array<Team>,
    private val download_api_key: String = """
""".trimIndent(),
    // wen requesting an update, we use this counter to count the number of updates received
    private var team_update_counter: AtomicInteger = AtomicInteger(0),
    private var team_update_in_progress: AtomicBoolean = AtomicBoolean(false),
    private val num_teams: Int = 6,

) : LiveData<LocalTime>(), DownloadCallback, UploadCallback {

    // Singelton pattern: getinstance
    // changed singelton to createinstance and getinstance, as only the first screen will create it
    companion object {
        @Volatile
        private var instance: TeamPositionsManager? = null

        fun getInstance(): TeamPositionsManager {
            return instance!!
        }

        fun check_if_instance_is_created():Boolean{
            return instance!=null
        }

        @MainThread
        fun createInstance(context: Context, selectedTeam: String) {
            assert(instance == null)


            val gson = GsonBuilder()
                .registerTypeAdapter(LocalTime::class.java, LocalTimeSerializer())
                .registerTypeAdapter(Team::class.java, TeamInstanceCreator())
                .create()

            val jsonString = loadJSONFromAsset(context, "empty_positions.json")
            val teams: Map<String, Team> = gson.fromJson(
                jsonString,
                object : TypeToken<Map<String, Team>>() {}.type
            )

            //val api_key_json = loadJSONFromAsset(context, "api_key.json")
            instance = TeamPositionsManager(
                selectedTeam,
                teams.values.toTypedArray(),
                //api_key_json!!
            )
            instance!!.request_updates()
        }

    }

    private fun getUpdateTime(): LocalTime {
        var teamPos = -1
        var team_found = false
        for (i in teamlist.indices) {
            if (currentTeam == teamlist[i].Name) {
                assert(!team_found)
                team_found = true
                teamPos = i
                //break
                // one could break but our assertion checks that we found only one match
            }
        }
        assert(team_found)

        if (teamlist[teamPos].Positions.isEmpty())
            return LocalTime.MIDNIGHT
        else
            return teamlist[teamPos].Positions.last().Time
    }

    fun getteamPositions(name: String): List<Position> {
        var teamPos = -1
        var team_found = false
        for (i in teamlist.indices) {
            if (name == teamlist[i].Name) {
                assert(!team_found)
                team_found = true
                teamPos = i
                //break
                // one could break but our assertion checks that we found only one match
            }
        }
        assert(team_found)

        val all_positions = teamlist[teamPos].Positions

        val upddateTime = getUpdateTime()
        var known_positions = all_positions.size

        for (i in all_positions.indices) {
            if (all_positions[i].Time.isAfter(upddateTime)) {

                Log.d("TeamManager", "Update is not visible")
                Log.d("TeamManager", upddateTime.toString())
                Log.d("TeamManager", all_positions[i].toString())
                known_positions = i
                break

            }
        }

        return all_positions.subList(0, known_positions)
    }

    fun getCurrentTeamName(): String {
        return if (currentTeam == "X")
            "MR X"
        else
            currentTeam
    }

    fun request_updates() {

        if(this.value== null || Duration.between(this.value!!, LocalTime.now())!!.seconds>R.dimen.minTimeBetweenJsonUpdates){
        // if not minTimeBetweenJsonUpdates has passed: do nothing
        if (team_update_in_progress.compareAndSet(false, true)) {
            // if no update in progress:
            for (t in teamlist) {
                enqueue_downloadJsonFromCloudStorage(download_api_key, t.Name + ".json", this)
            }
        }
        }
    }

    private fun finish_update() {
        val assertion = team_update_counter.compareAndSet(num_teams, 0)
        assert(assertion)
        val assertion2 = team_update_in_progress.compareAndSet(true, false)
        assert(assertion2)

        super.postValue(LocalTime.now())
    }

    override fun onDownloadComplete(result: String) {
        // Handle the downloaded string here

        // TODO should we save the gson reader instead of rebuilding it?
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalTime::class.java, LocalTimeSerializer())
            .registerTypeAdapter(Team::class.java, TeamInstanceCreator())
            .create()

        val teams: Map<String, Team> = gson.fromJson(
            result,
            object : TypeToken<Map<String, Team>>() {}.type
        )

        val team = teams.entries.first().value

        var team_found = false
        for (i in teamlist.indices) {
            if (team.Name == teamlist[i].Name) {
                assert(!team_found)
                team_found = true
                teamlist[i] = team
                //break
                // one could break but our assertion checks that we found only one match
            }
        }
        assert(team_found)

        val count = team_update_counter.incrementAndGet()
        if (count == num_teams) {
            finish_update()
        }
    }

    override fun onDownloadError(error: Exception) {
        // Handle the download error here

        Log.e("TeamManager", "Json Update Failed")
        if (error.message != null) {
            Log.e("TeamManager", error.message!!)
        }

        val count = team_update_counter.incrementAndGet()
        if (count == num_teams) {
            finish_update()
        }
    }

    fun add_position(pos: Position) {

        while (team_update_in_progress.get()){
            // wait for download to complete
            // in this case freezing the app is resonable as the user wants to wait for the update anyway
            Thread.sleep(100)
        }

        var teamPos = -1
        var team_found = false
        for (i in teamlist.indices) {
            if (currentTeam == teamlist[i].Name) {
                assert(!team_found)
                team_found = true
                teamPos = i
                //break
                // one could break but our assertion checks that we found only one match
            }
        }
        assert(team_found)

        val team = teamlist[teamPos]

        team.Positions.add(pos)

        // gives a json representation
        val json_string = team.tojson()
        enqueue_UploadJsonToCloudStorage(download_api_key, team.Name + ".json", json_string, this)
    }





    override fun onUploadComplete() {
        request_updates()
    }

    override fun onUploadError(error: Exception) {
        // Handle the download error here

        Log.e("TeamManager", "Json Update Failed")
        if (error.message != null) {
            Log.e("TeamManager", error.message!!)
        }

        val count = team_update_counter.incrementAndGet()
        if (count == num_teams) {
            finish_update()
        }
    }


}
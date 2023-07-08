package com.example.scotlandyardlive.ui.map

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.google.auth.oauth2.AccessToken
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
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


class MapManager {
    // Start Location: Frankfurt Hauptbahnhof
    private var map_center: IGeoPoint = GeoPoint(50.10688202021955, 8.66243955604308)
    private var zoom_level:Double = 14.0
    private var MapTileProvider: ITileSource
    //TODO does this do the coccect thle caching?

    private constructor(context: Context) {
        org.osmdroid.config.Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        org.osmdroid.config.Configuration.getInstance()
            .setUserAgentValue("ScotlandYardLiveApp_User_ID")
        MapTileProvider = TileSourceFactory.MAPNIK

    }

    public fun get_MapTileProvider():ITileSource{
        return MapTileProvider
    }

    public fun get_map_center():IGeoPoint{
        return map_center
    }

    public fun set_map_center(center:IGeoPoint){
        map_center=center
    }

    public fun get_zoom_level():Double{
        return zoom_level
    }

    public fun set_zoom_level(lvl:Double){
        zoom_level=lvl
    }


    // Singelton pattern: getinstance
    companion object {
        @Volatile
        private var instance: MapManager? = null

        fun getInstance(context: Context): MapManager {
            return instance ?: synchronized(this) {
                instance ?: MapManager(context).also { instance = it }
            }


        }
    }
}
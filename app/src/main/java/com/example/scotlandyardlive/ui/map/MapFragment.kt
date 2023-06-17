package com.example.scotlandyardlive.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ScaleDrawable
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.scotlandyardlive.R
import com.example.scotlandyardlive.StationMap
import com.example.scotlandyardlive.TeamPositionsManager
import com.example.scotlandyardlive.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import org.osmdroid.api.IGeoPoint
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var thiscontext: Context
    private lateinit var map: MapView

    // Start Location: Frankfurt Hauptbahnhof
    var pos_x: Double = 8.66243955604308 // lon
    var pos_y: Double = 50.10688202021955 // lat

    private lateinit var mapController: IMapController

    private lateinit var teamManager: TeamPositionsManager
    private lateinit var stationmap: StationMap
    private lateinit var marker_team_X: Marker
    private lateinit var marker_team_rot: Marker
    private lateinit var marker_team_grün: Marker
    private lateinit var marker_team_gelb: Marker
    private lateinit var marker_team_blau: Marker
    private lateinit var marker_team_orange: Marker


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapViewModel =
            ViewModelProvider(this).get(MapViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        thiscontext = container!!.context

        teamManager = TeamPositionsManager.getInstance()
        stationmap = StationMap.getInstance(requireContext())


        getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        map = binding.map

        map.setTileSource(TileSourceFactory.MAPNIK)

        map.setTilesScaledToDpi(true)
        map.setMultiTouchControls(true)


        marker_team_X = Marker(map)
        marker_team_rot = Marker(map)
        marker_team_grün = Marker(map)
        marker_team_gelb = Marker(map)
        marker_team_blau = Marker(map)
        marker_team_orange = Marker(map)

        // Add the markers to the map overlays
        map.overlays.add(marker_team_X)
        map.overlays.add(marker_team_rot)
        map.overlays.add(marker_team_grün)
        map.overlays.add(marker_team_gelb)
        map.overlays.add(marker_team_blau)
        map.overlays.add(marker_team_orange)

        mapController = map.controller
        mapController.setZoom(14.0)
        // Start Location: Frankfurt Hauptbahnhof
        val startPoint = GeoPoint(pos_y, pos_x);
        mapController.setCenter(startPoint);

        return root
    }

    override fun onResume() {
        super.onResume()

        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume() //needed for compass, my location overlays, v6.0.0 and up
        //map.overlays.clear()

        update_position_marker("X")
        update_position_marker("Rot")
        update_position_marker("Grün")
        update_position_marker("Gelb")
        update_position_marker("Blau")
        update_position_marker("Orange")

        // re-draw all the markers
        map.invalidate()

    }

    fun update_position_marker(teamname: String) {
        var color = resources.getColor(R.color.black)
        var marker = marker_team_X


        when (teamname) {
            "X" -> {
                color = resources.getColor(R.color.black)
                marker = marker_team_X
            }

            "Rot" -> {
                color = resources.getColor(R.color.red)
                marker = marker_team_rot
            }

            "Grün" -> {
                color = resources.getColor(R.color.green)
                marker = marker_team_grün
            }

            "Blau" -> {
                color = resources.getColor(R.color.blue)
                marker = marker_team_blau
            }

            "Gelb" -> {
                color = resources.getColor(R.color.yellow)
                marker = marker_team_gelb
            }

            "Orange" -> {
                resources.getColor(R.color.orange)
                marker = marker_team_orange
            }
        }

        val positions = teamManager.getteamPositions(teamname)

        if (positions.isEmpty()) {
            marker.setVisible(false)
        } else {
            val last_pos = positions.last()

            var vecicle_icon = resources.getDrawable(R.drawable.b_pic)

            when (last_pos.Transport) {
                "R" -> {
                    vecicle_icon = resources.getDrawable(R.drawable.r_pic)
                }

                "S" -> {
                    vecicle_icon = resources.getDrawable(R.drawable.s_pic)
                }

                "U" -> {
                    vecicle_icon = resources.getDrawable(R.drawable.u_pic)
                }

                "T" -> {
                    vecicle_icon = resources.getDrawable(R.drawable.t_pic)
                }

                "B" -> {
                    vecicle_icon = resources.getDrawable(R.drawable.b_pic)
                }
            }

            marker.icon = get_position_marker(color, vecicle_icon)

            val station_coords = stationmap.get_station_position(last_pos.Station)

            if (station_coords != null) {
                Log.d("SetMarkers", last_pos.toString())
                Log.d("SetMarkers", station_coords.toString())

                marker.position = GeoPoint(station_coords.second, station_coords.first)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                marker.title = last_pos.Time.format(formatter) + "h: " + last_pos.Station
                marker.setVisible(true)
            } else {
                marker.setVisible(false)
            }
        }

    }


    override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause()  //needed for compass, my location overlays, v6.0.0 and up

    }

    fun get_position_marker(color: Int, vehicle_icon: Drawable): Drawable {
        var loc_icon = resources.getDrawable(R.drawable.baseline_location_on_24)
        val mutableDrawable = loc_icon?.mutate()
        mutableDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)


        // Create an array of drawables for the layers
        val layers = arrayOf(mutableDrawable, vehicle_icon)

        // Create a LayerDrawable with the layers array
        val layerDrawable = LayerDrawable(layers)

        // Set the position of the foreground layer
        val foregroundIndex = 1  // Index of the foreground layer in the layers array
        val leftOffset = 40   // Left offset in pixels
        val topOffset = 20     // Top offset in pixels
        val rightOffset = 38    // Top offset in pixels
        val bottomOffset = 52     // Top offset in pixels
        layerDrawable.setLayerInset(
            foregroundIndex,
            leftOffset,
            topOffset,
            rightOffset,
            bottomOffset
        )

        return layerDrawable
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
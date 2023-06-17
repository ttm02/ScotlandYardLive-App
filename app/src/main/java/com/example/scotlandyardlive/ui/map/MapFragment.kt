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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.scotlandyardlive.R
import com.example.scotlandyardlive.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import org.osmdroid.config.Configuration.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay
import kotlin.math.roundToInt


class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var thiscontext: Context
    private lateinit var map : MapView

    //TODO do we need to use only one  Location client?
    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Start Location: Frankfurt Hauptbahnhof
    var pos_x: Double = 8.66243955604308 // lon
    var pos_y: Double = 50.10688202021955 // lat


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

        //TODO handle the permissions
        //handle permissions first, before map is created. not depicted here

        getInstance().load(thiscontext, PreferenceManager.getDefaultSharedPreferences(thiscontext))

        map = binding.map

        map.setTileSource(TileSourceFactory.MAPNIK)

        map.setTilesScaledToDpi(true)
        map.setMultiTouchControls(true)


        var position_marker= Marker(map)

        val icon = resources.getDrawable(R.drawable.b_pic)
        val color = resources.getColor(R.color.red)
        val loc_icon = get_position_marker(color,icon)



        position_marker.icon = loc_icon
        position_marker.position=GeoPoint(pos_y, pos_x)
        position_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        position_marker.setVisible(true)
        map.overlays.add(position_marker);






        val mapController = map.controller
        mapController.setZoom(14.0)
        // Start Location: Frankfurt Hauptbahnhof
        val startPoint = GeoPoint(pos_y, pos_x);
        mapController.setCenter(startPoint);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(thiscontext)
        if (ActivityCompat.checkSelfPermission(
                thiscontext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MapFragment.LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            requestLocation()
        }

        return root
    }

    private fun requestLocation() {

        try {
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token

                    override fun isCancellationRequested() = false
                })
                .addOnSuccessListener { location: Location? ->
                    location?.let {

                        if (location != null) {
                            // Handle location here
                            pos_y = it.latitude
                            pos_x = it.longitude
                            //TODO update map?
                        }else
                            Toast.makeText(thiscontext, "cannot get Location", Toast.LENGTH_SHORT).show()
                    }
                }

        }
        catch (s: SecurityException){
            Toast.makeText(thiscontext, "no permission to get location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause()  //needed for compass, my location overlays, v6.0.0 and up
    }

    fun get_position_marker(color: Int, vehicle_icon: Drawable):Drawable{
        var loc_icon =resources.getDrawable(R.drawable.baseline_location_on_24)
        val mutableDrawable = loc_icon?.mutate()
        mutableDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        //val scaled_vehicle = ScaleDrawable(vehicle_icon, 0, 12f, 12f).drawable

        // Create an array of drawables for the layers
        val layers = arrayOf(mutableDrawable, vehicle_icon)
        //val layers = arrayOf(vehicle_icon,mutableDrawable)

    // Create a LayerDrawable with the layers array
        val layerDrawable = LayerDrawable(layers)

        // Set the position of the foreground layer
        val foregroundIndex = 1  // Index of the foreground layer in the layers array
        val leftOffset = 40   // Left offset in pixels
        val topOffset = 20     // Top offset in pixels
        val rightOffset = 38    // Top offset in pixels
        val bottomOffset = 52     // Top offset in pixels
        layerDrawable.setLayerInset(foregroundIndex, leftOffset, topOffset, rightOffset, bottomOffset)

        return layerDrawable
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}
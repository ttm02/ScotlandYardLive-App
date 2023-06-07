package com.example.scotlandyardlive.ui.map

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.scotlandyardlive.databinding.FragmentHomeBinding
import com.example.scotlandyardlive.databinding.FragmentMapBinding
import org.osmdroid.config.Configuration.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var thiscontext: Context
    private lateinit var map : MapView

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

        val mapController = map.controller
        mapController.setZoom(14.0)
        // Start Location: Frankfurt Hauptbahnhof
        val startPoint = GeoPoint(50.10688202021955, 8.66243955604308);
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
    }

    override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause()  //needed for compass, my location overlays, v6.0.0 and up
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
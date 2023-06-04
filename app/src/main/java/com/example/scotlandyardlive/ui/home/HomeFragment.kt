package com.example.scotlandyardlive.ui.home


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.scotlandyardlive.R
import com.example.scotlandyardlive.StationMap
import com.example.scotlandyardlive.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var thiscontext: Context
    private lateinit var spinner_group_selection: Spinner
    private lateinit var buttonR: RadioButton
    private lateinit var buttonS: RadioButton
    private lateinit var buttonU: RadioButton
    private lateinit var buttonT: RadioButton
    private lateinit var buttonB: RadioButton
    private lateinit var buttonM: RadioButton

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Start Location: Frankfurt Hauptbahnhof
    var pos_x: Double = 8.66243955604308 // lon
    var pos_y: Double = 50.10688202021955 // lat

    private lateinit var stationtextview: AutoCompleteTextView
    private lateinit var buttonLocate: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        thiscontext = container!!.context
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val data = thiscontext.resources.getStringArray(R.array.groups)
        spinner_group_selection = binding.spinnerGroupSelection

        // Create an ArrayAdapter using the array and default spinner layout
        val adapter = ArrayAdapter(thiscontext, android.R.layout.simple_spinner_item, data)

        // Set the dropdown layout style
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinner_group_selection.adapter = adapter


        // the radio buttons
        buttonS = binding.radioButtonS
        buttonR = binding.radioButtonR
        buttonU = binding.radioButtonU
        buttonT = binding.radioButtonT
        buttonB = binding.radioButtonB
        buttonM = binding.radioButtonM

        buttonS.setOnClickListener {
            buttonS.isChecked = true
            buttonR.isChecked = false
            buttonU.isChecked = false
            buttonT.isChecked = false
            buttonB.isChecked = false
            buttonM.isChecked = false
        }

        buttonR.setOnClickListener {
            buttonS.isChecked = false
            buttonR.isChecked = true
            buttonU.isChecked = false
            buttonT.isChecked = false
            buttonB.isChecked = false
            buttonM.isChecked = false
        }

        buttonU.setOnClickListener {
            buttonS.isChecked = false
            buttonR.isChecked = false
            buttonU.isChecked = true
            buttonT.isChecked = false
            buttonB.isChecked = false
            buttonM.isChecked = false
        }

        buttonT.setOnClickListener {
            buttonS.isChecked = false
            buttonR.isChecked = false
            buttonU.isChecked = false
            buttonT.isChecked = true
            buttonB.isChecked = false
            buttonM.isChecked = false
        }

        buttonB.setOnClickListener {
            buttonS.isChecked = false
            buttonR.isChecked = false
            buttonU.isChecked = false
            buttonT.isChecked = false
            buttonB.isChecked = true
            buttonM.isChecked = false
        }

        buttonM.setOnClickListener {
            buttonS.isChecked = false
            buttonR.isChecked = false
            buttonU.isChecked = false
            buttonT.isChecked = false
            buttonB.isChecked = false
            buttonM.isChecked = true
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(thiscontext)

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                thiscontext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            requestLocation()
        }

        Toast.makeText(thiscontext," Test Text",Toast.LENGTH_SHORT).show()




        stationtextview = binding.autoCompleteTextView

        var stations = StationMap.getInstance(thiscontext)
        val adapter_stations = ArrayAdapter(
            thiscontext,
            android.R.layout.simple_dropdown_item_1line, stations.get_station_list()
        )

        val nearest_station = stations.get_nearest_station(Pair(pos_x,pos_y))

        stationtextview.setAdapter(adapter_stations)
        stationtextview.setText(nearest_station)

        buttonLocate = binding.imageButton

        buttonLocate.setOnClickListener {
            stationtextview.setText("---Locate---")
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
                            var stations = StationMap.getInstance(thiscontext)
                            val nearest_station = stations.get_nearest_station(Pair(pos_x, pos_y))
                            stationtextview.setText(nearest_station)

                        }else
                            Toast.makeText(thiscontext, "cannot get Location", Toast.LENGTH_SHORT).show()
                    }
                }

        }
        catch (s: SecurityException){
            Toast.makeText(thiscontext, "no permission to get location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation()
            } else {
                // Permission denied
                // Handle accordingly (e.g., show a message, disable location features)
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }


    override fun onStart() {
        super.onStart()
        /**/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
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
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.scotlandyardlive.Position
import com.example.scotlandyardlive.R
import com.example.scotlandyardlive.StationMap
import com.example.scotlandyardlive.TeamPositionsManager
import com.example.scotlandyardlive.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.time.LocalTime


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var thiscontext: Context
    private lateinit var teamText: TextView
    private lateinit var buttonR: RadioButton
    private lateinit var buttonS: RadioButton
    private lateinit var buttonU: RadioButton
    private lateinit var buttonT: RadioButton
    private lateinit var buttonB: RadioButton
    //private lateinit var buttonM: RadioButton

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Start Location: Frankfurt Hauptbahnhof
    var pos_x: Double = 8.66243955604308 // lon
    var pos_y: Double = 50.10688202021955 // lat

    private lateinit var stationtextview: AutoCompleteTextView
    private lateinit var buttonLocate: ImageButton

    private lateinit var stationsuggest1: TextView
    private lateinit var stationsuggest2: TextView
    private lateinit var stationsuggest3: TextView
    private lateinit var stationsuggest4: TextView
    private lateinit var stationsuggest5: TextView

    private lateinit var stationsuggest_val1: String
    private lateinit var stationsuggest_val2: String
    private lateinit var stationsuggest_val3: String
    private lateinit var stationsuggest_val4: String
    private lateinit var stationsuggest_val5: String


    private lateinit var sendButton: Button
    private lateinit var teamposManager: TeamPositionsManager

    private lateinit var position_check_box: CheckBox

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
        teamText = binding.textViewGroup

        teamposManager = TeamPositionsManager.getInstance()

        teamText.text = teamposManager.getCurrentTeamName()



        stationsuggest1 = binding.stationSuggest1
        stationsuggest2 = binding.stationSuggest2
        stationsuggest3 = binding.stationSuggest3
        stationsuggest4 = binding.stationSuggest4
        stationsuggest5 = binding.stationSuggest5


        // the radio buttons
        buttonS = binding.radioButtonS
        buttonR = binding.radioButtonR
        buttonU = binding.radioButtonU
        buttonT = binding.radioButtonT
        buttonB = binding.radioButtonB
        //buttonM = binding.radioButtonM

        buttonS.setOnClickListener {
            buttonS.isChecked = true
            buttonR.isChecked = false
            buttonU.isChecked = false
            buttonT.isChecked = false
            buttonB.isChecked = false
            //buttonM.isChecked = false
        }

        buttonR.setOnClickListener {
            buttonS.isChecked = false
            buttonR.isChecked = true
            buttonU.isChecked = false
            buttonT.isChecked = false
            buttonB.isChecked = false
            //buttonM.isChecked = false
        }

        buttonU.setOnClickListener {
            buttonS.isChecked = false
            buttonR.isChecked = false
            buttonU.isChecked = true
            buttonT.isChecked = false
            buttonB.isChecked = false
            //buttonM.isChecked = false
        }

        buttonT.setOnClickListener {
            buttonS.isChecked = false
            buttonR.isChecked = false
            buttonU.isChecked = false
            buttonT.isChecked = true
            buttonB.isChecked = false
            //buttonM.isChecked = false
        }

        buttonB.setOnClickListener {
            buttonS.isChecked = false
            buttonR.isChecked = false
            buttonU.isChecked = false
            buttonT.isChecked = false
            buttonB.isChecked = true
            //buttonM.isChecked = false
        }

        /*
        buttonM.setOnClickListener {
            buttonS.isChecked = false
            buttonR.isChecked = false
            buttonU.isChecked = false
            buttonT.isChecked = false
            buttonB.isChecked = false
            buttonM.isChecked = true
        }
        */

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




        stationtextview = binding.autoCompleteTextView

        var stations = StationMap.getInstance(thiscontext)
        val adapter_stations = ArrayAdapter(
            thiscontext,
            android.R.layout.simple_dropdown_item_1line, stations.get_station_list()
        )

        stationtextview.setAdapter(adapter_stations)

        buttonLocate = binding.imageButton

        buttonLocate.setOnClickListener {
            stationtextview.setText("---Locate---")
            requestLocation()
        }

        stationsuggest1.setOnClickListener { stationtextview.setText(stationsuggest_val1) }
        stationsuggest2.setOnClickListener { stationtextview.setText(stationsuggest_val2) }
        stationsuggest3.setOnClickListener { stationtextview.setText(stationsuggest_val3) }
        stationsuggest4.setOnClickListener { stationtextview.setText(stationsuggest_val4) }
        stationsuggest5.setOnClickListener { stationtextview.setText(stationsuggest_val5) }

        sendButton = binding.buttonSendPos
        sendButton.setOnClickListener { send_location_update() }



        position_check_box = binding.checkBox

        if (teamposManager.getCurrentTeamName() != "MR X") {
            position_check_box.isChecked = true
            position_check_box.isEnabled = false
        }
        return root
    }

    private fun update_station_suggestion() {
        var stations = StationMap.getInstance(thiscontext)
        val nearest_stations = stations.get_nearest_stations(Pair(pos_x, pos_y), 5)


        stationsuggest1.setText("${nearest_stations[0].first} (${nearest_stations[0].second}m)")
        stationsuggest_val1 = nearest_stations[0].first
        stationsuggest2.setText("${nearest_stations[1].first} (${nearest_stations[1].second}m)")
        stationsuggest_val2 = nearest_stations[1].first
        stationsuggest3.setText("${nearest_stations[2].first} (${nearest_stations[2].second}m)")
        stationsuggest_val3 = nearest_stations[2].first
        stationsuggest4.setText("${nearest_stations[3].first} (${nearest_stations[3].second}m)")
        stationsuggest_val4 = nearest_stations[3].first
        stationsuggest5.setText("${nearest_stations[4].first} (${nearest_stations[4].second}m)")
        stationsuggest_val5 = nearest_stations[4].first
        //stationtextview.setText(nearest_station)

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
                            update_station_suggestion()
                        } else
                            Toast.makeText(thiscontext, "cannot get Location", Toast.LENGTH_SHORT)
                                .show()
                    }
                }

        } catch (s: SecurityException) {
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

    private fun send_location_update() {

        var transport = "-"
        if (buttonR.isChecked) {
            assert(transport == "-")
            transport = "R"
        } else if (buttonB.isChecked) {
            assert(transport == "-")
            transport = "B"
        } else if (buttonU.isChecked) {
            assert(transport == "-")
            transport = "U"
        } else if (buttonS.isChecked) {
            assert(transport == "-")
            transport = "S"
        } else if (buttonT.isChecked) {
            assert(transport == "-")
            transport = "T"
        }
        assert(transport != "-")


        var station = stationtextview.text.toString()

        if (teamposManager.getCurrentTeamName() == "MR X" && !position_check_box.isChecked) {
            station = "hidden"
        }
        val new_loc: Position = Position(LocalTime.now(), station, transport)

        teamposManager.add_position(new_loc)

        if (teamposManager.getCurrentTeamName() == "MR X" && !position_check_box.isChecked) {
            Toast.makeText(requireContext(), "Umstieg ohne Position Gemeldet", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Position Gemeldet", Toast.LENGTH_SHORT).show()
        }

        // go to dashboard
        findNavController().navigate(R.id.action_navigation_home_to_navigation_dashboard)
    }


}
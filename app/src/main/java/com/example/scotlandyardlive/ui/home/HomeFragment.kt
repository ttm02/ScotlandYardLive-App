package com.example.scotlandyardlive.ui.home


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.scotlandyardlive.R
import com.example.scotlandyardlive.databinding.FragmentHomeBinding


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

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

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


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
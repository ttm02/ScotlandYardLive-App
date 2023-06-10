package com.example.scotlandyardlive

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.scotlandyardlive.databinding.FragmentTeamSelectionBinding
import com.example.scotlandyardlive.databinding.FragmentTourDetailBinding


class TeamSelectionFragment : Fragment() {

    private var _binding: FragmentTeamSelectionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var statusTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentTeamSelectionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        statusTextView=binding.textViewMsg

        val butx=binding.buttonX
        butx.setOnClickListener { select_team("X") }
        val butRot = binding.buttonRot
        butRot.setOnClickListener { select_team("Rot") }

        val butGrün = binding.buttonGruen
        butGrün.setOnClickListener { select_team("Grün") }

        val butGelb = binding.buttonGelb
        butGelb.setOnClickListener { select_team("Gelb") }

        val butBlau = binding.buttonBlau
        butBlau.setOnClickListener { select_team("Blau") }

        val butOrange = binding.buttonOrange
        butOrange.setOnClickListener { select_team("Orange") }


        // Inflate the layout for this fragment
        return root


    }

    override fun onResume() {
        super.onResume()
        if (TeamPositionsManager.check_if_instance_is_created()){
            // a team was already selected
            val navController = findNavController()
            navController.navigate(R.id.action_teamSelectionFragment_to_navigation_home)
        }

    }

    fun select_team(team: String){

        TeamPositionsManager.createdInstance(requireContext(),team)
        val navController = findNavController()
        navController.navigate(R.id.action_teamSelectionFragment_to_navigation_home)

    }


}
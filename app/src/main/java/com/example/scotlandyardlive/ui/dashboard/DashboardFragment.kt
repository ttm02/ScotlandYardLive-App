package com.example.scotlandyardlive.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextClock
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.scotlandyardlive.R
import com.example.scotlandyardlive.Team
import com.example.scotlandyardlive.TeamLocations
import com.example.scotlandyardlive.databinding.FragmentDashboardBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


data class TeamView_row(
    val team_name:TextView,
    val time: TextView,
    val image: ImageView,
    val stationText: TextView
)

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var thiscontext: Context

    private lateinit var teamlocations : TeamLocations

    private lateinit var teamview_x:TeamView_row
    private lateinit var teamview_rot:TeamView_row
    private lateinit var teamview_blau:TeamView_row
    private lateinit var teamview_grün:TeamView_row
    private lateinit var teamview_gelb:TeamView_row
    private lateinit var teamview_orange:TeamView_row

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        thiscontext = container!!.context

        teamlocations= TeamLocations.getInstance(thiscontext)

        teamview_x= TeamView_row(binding.textView11,binding.textClock1,binding.imageView1,binding.textView1)
        teamview_rot=TeamView_row(binding.textView12,binding.textClock2,binding.imageView2,binding.textView2)
        teamview_blau=TeamView_row(binding.textView13,binding.textClock3,binding.imageView3,binding.textView3)
        teamview_grün=TeamView_row(binding.textView14,binding.textClock4,binding.imageView4,binding.textView4)
        teamview_gelb=TeamView_row(binding.textView15,binding.textClock5,binding.imageView5,binding.textView5)
        teamview_orange=TeamView_row(binding.textView16,binding.textClock6,binding.imageView6,binding.textView6)


        set_team_veiws()
        return root
    }

    fun set_team_veiw( t: Team,  row:TeamView_row){

        if (t.Positions.isNotEmpty()){

        val last_station = t.Positions.last()

        Log.d("set_team_veiw", last_station.toString())


            // time
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            row.time.text = last_station.Time.format(formatter)

            // picture
            when (last_station.Transport) {
                "R" -> {
                    row.image.setImageResource(R.drawable.r_pic)
                }

                "S" -> {
                    row.image.setImageResource(R.drawable.s_pic)
                }

                "U" -> {
                    row.image.setImageResource(R.drawable.u_pic)
                }

                "T" -> {
                    row.image.setImageResource(R.drawable.t_pic)
                }

                "B" -> {
                    row.image.setImageResource(R.drawable.b_pic)
                }

                else -> {
                    row.image.setImageResource(R.drawable.b_pic)
                }
            }

            // station
            row.stationText.text = last_station.Station
        }else{
            row.stationText.text = "no known location"
        }
    }

    fun set_team_veiws(){

        set_team_veiw(teamlocations.getteams()[0],teamview_x)
        set_team_veiw(teamlocations.getteams()[1],teamview_rot)
        set_team_veiw(teamlocations.getteams()[2],teamview_blau)
        set_team_veiw(teamlocations.getteams()[3],teamview_grün)
        set_team_veiw(teamlocations.getteams()[4],teamview_gelb)
        set_team_veiw(teamlocations.getteams()[5],teamview_orange)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.scotlandyardlive

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.scotlandyardlive.databinding.FragmentTourDetailBinding
import java.time.format.DateTimeFormatter


class TourDetailFragment : Fragment() {

    private var _binding: FragmentTourDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var tableLayout: TableLayout
    private lateinit var captionText: TextView

    private lateinit var teamManager: TeamPositionsManager
    private lateinit var Teamname: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTourDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root

        Teamname = requireArguments().getString("Team")!!


        tableLayout = binding.tableLayout
        captionText = binding.caption
        teamManager = TeamPositionsManager.getInstance()



        return root

    }


    override fun onResume() {
        super.onResume()

        var team_string_caption = Teamname
        if (Teamname == "X")
            team_string_caption = "MR X"

        captionText.setText(
            resources.getString(R.string.historie_caption).format(team_string_caption)
        )

        // clear
        tableLayout.removeAllViews()

        val teamPositions = teamManager.getteamPositions(Teamname)

        val context = requireContext()
        for (position in teamPositions) {

            val row = TableRow(context)

            val layout = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            layout.setMargins(0, 10, 0, 0)
            row.layoutParams = layout

            //TODO this is some duplicate code...

            val time = TextView(context)
            val image = ImageView(context)
            val stationText = TextView(context)

            // Layout Parameters
            val layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val textSizeInSp = resources.getDimension(R.dimen.font_size) / 2

            val scale = resources.displayMetrics.scaledDensity
            val textSizeInPx = (textSizeInSp * scale).toInt()

            time.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx.toFloat())
            stationText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx.toFloat())
            time.layoutParams = layoutParams
            stationText.layoutParams = layoutParams

            // time
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            time.text = position.Time.format(formatter)

            // picture
            when (position.Transport) {
                "R" -> {
                    image.setImageResource(R.drawable.r_pic)
                }

                "S" -> {
                    image.setImageResource(R.drawable.s_pic)
                }

                "U" -> {
                    image.setImageResource(R.drawable.u_pic)
                }

                "T" -> {
                    image.setImageResource(R.drawable.t_pic)
                }

                "M" -> {
                    image.setImageResource(R.drawable.m_pic)
                }

                "B" -> {
                    image.setImageResource(R.drawable.b_pic)
                }

                else -> {
                    image.setImageResource(R.drawable.b_pic)
                }
            }

            // station
            stationText.text = position.Station




            row.addView(time)
            row.addView(image)
            row.addView(stationText)
            tableLayout.addView(row)

        }
        // the hex value
        var color = resources.getColor(R.color.black)

        when (Teamname) {
            "X" -> color = resources.getColor(R.color.black)
            "Rot" -> color = resources.getColor(R.color.red)
            "Grün" -> color = resources.getColor(R.color.green)
            "Blau" -> color = resources.getColor(R.color.blue)
            "Gelb" -> color = resources.getColor(R.color.yellow)
            "Orange" -> color = resources.getColor(R.color.orange)
        }

//            activity?.actionBar?.setBackgroundDrawable(ColorDrawable(color))
        activity?.window?.statusBarColor = color
        activity?.window?.navigationBarColor = color


    }

    override fun onDetach() {
        super.onDetach()


        var color = resources.getColor(R.color.purple_500)

        // this does not work :-(
        //activity?.actionBar?.setBackgroundDrawable(ColorDrawable(color))
        activity?.window?.statusBarColor = color
        activity?.window?.navigationBarColor = color
    }
}
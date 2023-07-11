package com.punchthrough.blestarterappandroid

import android.content.Context
import android.os.Bundle
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.punchthrough.blestarterappandroid.DistanceFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.punchthrough.blestarterappandroid.R
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [DistanceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DistanceFragment : Fragment() {
    private lateinit var sp : SharedPreferences

    private lateinit var dailyTextView: TextView

    private lateinit var dailyBarChart : BarChart

    private var barEntries : ArrayList<BarEntry> = ArrayList()

    private val chartHandler = Handler(Looper.getMainLooper())

    private val textHandler = Handler(Looper.getMainLooper())

    private lateinit var device1Attach : View
    private lateinit var device2Attach : View
    private lateinit var device3Attach : View
    private lateinit var device4Attach : View
    private lateinit var device5Attach : View
    private lateinit var device6Attach : View
    private lateinit var device7Attach : View

    private lateinit var device1ProgressBar : View
    private lateinit var device2ProgressBar : View
    private lateinit var device3ProgressBar : View
    private lateinit var device4ProgressBar : View
    private lateinit var device5ProgressBar : View
    private lateinit var device6ProgressBar : View
    private lateinit var device7ProgressBar : View


    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_distance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dailyTextView = requireView().findViewById(R.id.power_generated_daily)
        dailyBarChart = requireView().findViewById(R.id.daily_wattage_bar_chart)
        sp = activity?.getSharedPreferences("AppData", Context.MODE_PRIVATE)!!

        device1Attach = requireView().findViewById(R.id.device1_attach)
        device2Attach = requireView().findViewById(R.id.device2_attach)
        device3Attach = requireView().findViewById(R.id.device3_attach)
        device4Attach = requireView().findViewById(R.id.device4_attach)
        device5Attach = requireView().findViewById(R.id.device5_attach)
        device6Attach = requireView().findViewById(R.id.device6_attach)
        device7Attach = requireView().findViewById(R.id.device7_attach)

        device1ProgressBar = requireView().findViewById(R.id.device1_progress_bar)
        device2ProgressBar = requireView().findViewById(R.id.device2_progress_bar)
        device3ProgressBar = requireView().findViewById(R.id.device3_progress_bar)
        device4ProgressBar = requireView().findViewById(R.id.device4_progress_bar)
        device5ProgressBar = requireView().findViewById(R.id.device5_progress_bar)
        device6ProgressBar = requireView().findViewById(R.id.device6_progress_bar)
        device7ProgressBar = requireView().findViewById(R.id.device7_progress_bar)

        Thread {
            val calendar = Calendar.getInstance();
            var currentHour = 0

            createBarChart()
            while (true) {
                if (currentHour != calendar.get(Calendar.HOUR_OF_DAY)) {
                    createBarChart()
                    currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                }

                SystemClock.sleep(5000)
            }
        }.start()

        Thread {
            var dailyWattage = sp.getInt("TotalLog_daily_log", 0)

            while (true) {
                if (dailyWattage != sp.getInt("TotalLog_daily_log", dailyWattage)) {
                    dailyWattage = sp.getInt("TotalLog_daily_log", dailyWattage)
                }

                textHandler.post{
                    dailyTextView.text = StringBuilder().append(dailyWattage).append("wH").toString()

                    if (dailyWattage == 0) {
                        device1Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device2Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device3Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device4Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device5Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device6Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device7Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)

                        device1ProgressBar.visibility = View.GONE
                        device2ProgressBar.visibility = View.GONE
                        device3ProgressBar.visibility = View.GONE
                        device4ProgressBar.visibility = View.GONE
                        device5ProgressBar.visibility = View.GONE
                        device6ProgressBar.visibility = View.GONE
                        device7ProgressBar.visibility = View.GONE
                    }

                    if (dailyWattage >= 10) {
                        device1ProgressBar.visibility = View.VISIBLE
                        device1Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (dailyWattage >= 20) {
                        device2ProgressBar.visibility = View.VISIBLE
                        device2Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (dailyWattage >= 60) {
                        device3ProgressBar.visibility = View.VISIBLE
                        device3Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (dailyWattage >= 70) {
                        device4ProgressBar.visibility = View.VISIBLE
                        device4Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (dailyWattage >= 100) {
                        device5ProgressBar.visibility = View.VISIBLE
                        device5Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (dailyWattage >= 200) {
                        device6ProgressBar.visibility = View.VISIBLE
                        device6Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (dailyWattage >= 600) {
                        device7ProgressBar.visibility = View.VISIBLE
                        device7Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                }
                SystemClock.sleep(3000)
            }

        }.start()
    }

    private fun createBarChart() {
        getEntries()

        val barDataSet = BarDataSet(barEntries, "Wattage Generated (mWh)")
        barDataSet.setColor(ColorTemplate.rgb("45E5F7"))

        val data = BarData(barDataSet)
        chartHandler.post {
            dailyBarChart.data = data
            dailyBarChart.axisRight?.isEnabled = false
            dailyBarChart.description?.isEnabled = false

            dailyBarChart.setScaleEnabled(false)
            dailyBarChart.isDoubleTapToZoomEnabled = false

            dailyBarChart.xAxis?.valueFormatter = MyXAxisFormatter()
            dailyBarChart.axisLeft?.axisMinimum = 0f
            dailyBarChart.invalidate()
        }
    }




    class MyXAxisFormatter : ValueFormatter() {
        private val days = arrayOf("", "1AM", "2AM", "3AM", "4AM", "5AM", "6AM",
            "7AM", "8AM", "9AM", "10AM", "11AM", "12PM",
            "1PM", "2PM", "3PM", "4PM", "5PM", "6PM",
            "7PM", "8PM", "9PM", "10PM", "11PM", "12PM")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()) ?: value.toString()
        }
    }

    private fun getEntries() {
        barEntries.add(BarEntry(1f, sp.getInt("TotalLog_hour1_log", 0).toFloat()))
        barEntries.add(BarEntry(2f, sp.getInt("TotalLog_hour2_log", 0).toFloat()))
        barEntries.add(BarEntry(3f, sp.getInt("TotalLog_hour3_log", 0).toFloat()))
        barEntries.add(BarEntry(4f, sp.getInt("TotalLog_hour4_log", 0).toFloat()))
        barEntries.add(BarEntry(5f, sp.getInt("TotalLog_hour5_log", 0).toFloat()))
        barEntries.add(BarEntry(6f, sp.getInt("TotalLog_hour6_log", 0).toFloat()))
        barEntries.add(BarEntry(7f, sp.getInt("TotalLog_hour7_log", 0).toFloat()))
        barEntries.add(BarEntry(8f, sp.getInt("TotalLog_hour8_log", 0).toFloat()))
        barEntries.add(BarEntry(9f, sp.getInt("TotalLog_hour9_log", 0).toFloat()))
        barEntries.add(BarEntry(10f, sp.getInt("TotalLog_hour10_log", 0).toFloat()))
        barEntries.add(BarEntry(11f, sp.getInt("TotalLog_hour11_log", 0).toFloat()))
        barEntries.add(BarEntry(12f, sp.getInt("TotalLog_hour12_log", 0).toFloat()))
        barEntries.add(BarEntry(13f, sp.getInt("TotalLog_hour13_log", 0).toFloat()))
        barEntries.add(BarEntry(14f, sp.getInt("TotalLog_hour14_log", 0).toFloat()))
        barEntries.add(BarEntry(15f, sp.getInt("TotalLog_hour15_log", 0).toFloat()))
        barEntries.add(BarEntry(16f, sp.getInt("TotalLog_hour16_log", 0).toFloat()))
        barEntries.add(BarEntry(17f, sp.getInt("TotalLog_hour17_log", 0).toFloat()))
        barEntries.add(BarEntry(18f, sp.getInt("TotalLog_hour18_log", 0).toFloat()))
        barEntries.add(BarEntry(19f, sp.getInt("TotalLog_hour19_log", 0).toFloat()))
        barEntries.add(BarEntry(20f, sp.getInt("TotalLog_hour20_log", 0).toFloat()))
        barEntries.add(BarEntry(21f, sp.getInt("TotalLog_hour21_log", 0).toFloat()))
        barEntries.add(BarEntry(22f, sp.getInt("TotalLog_hour22_log", 0).toFloat()))
        barEntries.add(BarEntry(23f, sp.getInt("TotalLog_hour23_log", 0).toFloat()))
        barEntries.add(BarEntry(24f, sp.getInt("TotalLog_hour24_log", 0).toFloat()))
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DistanceFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): DistanceFragment {
            val fragment = DistanceFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
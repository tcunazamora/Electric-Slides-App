package com.punchthrough.blestarterappandroid

import android.content.Context
import android.content.SharedPreferences
import android.opengl.Visibility
import android.widget.TextView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import org.jetbrains.anko.backgroundDrawable
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {
    private lateinit var sp : SharedPreferences

    private lateinit var toDateTextView : TextView

    private var powerBarChart: BarChart? = null

    private val barEntries : ArrayList<BarEntry> = ArrayList()

    private val chartHandler = Handler(Looper.getMainLooper())

    private val textHandler = Handler(Looper.getMainLooper())

    private lateinit var device1Attach : View
    private lateinit var device2Attach : View
    private lateinit var device3Attach : View
    private lateinit var device4Attach : View
    private lateinit var device5Attach : View
    private lateinit var device6Attach : View
    private lateinit var device7Attach : View
    private lateinit var device8Attach : View
    private lateinit var device9Attach : View
    private lateinit var device10Attach : View

    private lateinit var device1ProgressBar : View
    private lateinit var device2ProgressBar : View
    private lateinit var device3ProgressBar : View
    private lateinit var device4ProgressBar : View
    private lateinit var device5ProgressBar : View
    private lateinit var device6ProgressBar : View
    private lateinit var device7ProgressBar : View
    private lateinit var device8ProgressBar : View
    private lateinit var device9ProgressBar : View
    private lateinit var device10ProgressBar : View

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
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toDateTextView = requireView().findViewById(R.id.power_generated_to_date)
        powerBarChart = requireView().findViewById(R.id.weekly_wattage_bar_chart)

        sp = activity?.getSharedPreferences("AppData", Context.MODE_PRIVATE)!!

        device1Attach = requireView().findViewById(R.id.device1_attach)
        device2Attach = requireView().findViewById(R.id.device2_attach)
        device3Attach = requireView().findViewById(R.id.device3_attach)
        device4Attach = requireView().findViewById(R.id.device4_attach)
        device5Attach = requireView().findViewById(R.id.device5_attach)
        device6Attach = requireView().findViewById(R.id.device6_attach)
        device7Attach = requireView().findViewById(R.id.device7_attach)
        device8Attach = requireView().findViewById(R.id.device8_attach)
        device9Attach = requireView().findViewById(R.id.device9_attach)
        device10Attach = requireView().findViewById(R.id.device10_attach)

        device1ProgressBar = requireView().findViewById(R.id.device1_progress_bar)
        device2ProgressBar = requireView().findViewById(R.id.device2_progress_bar)
        device3ProgressBar = requireView().findViewById(R.id.device3_progress_bar)
        device4ProgressBar = requireView().findViewById(R.id.device4_progress_bar)
        device5ProgressBar = requireView().findViewById(R.id.device5_progress_bar)
        device6ProgressBar = requireView().findViewById(R.id.device6_progress_bar)
        device7ProgressBar = requireView().findViewById(R.id.device7_progress_bar)
        device8ProgressBar = requireView().findViewById(R.id.device8_progress_bar)
        device9ProgressBar = requireView().findViewById(R.id.device9_progress_bar)
        device10ProgressBar = requireView().findViewById(R.id.device10_progress_bar)

        Thread {
            val calendar = Calendar.getInstance()
            var currentDay = 0

            createBarChart()
            while (true) {
                if (currentDay != calendar.get(Calendar.DAY_OF_WEEK)) {
                    createBarChart()
                    currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                }

                SystemClock.sleep(5000)
            }
        }.start()

        Thread {
            var toDateWattage = sp.getInt("TotalLog_to_date_log", 0)

            while (true) {
                if (toDateWattage != sp.getInt("TotalLog_to_date_log", toDateWattage)) {
                    toDateWattage = sp.getInt("TotalLog_to_date_log", toDateWattage)
                }

                textHandler.post{
                    toDateTextView.text = StringBuilder().append(toDateWattage).append("wH").toString()

                    if (toDateWattage == 0) {
                        device1Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device2Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device3Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device4Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device5Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device6Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device7Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device8Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device9Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)
                        device10Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach_empty)

                        device1ProgressBar.visibility = View.GONE
                        device2ProgressBar.visibility = View.GONE
                        device3ProgressBar.visibility = View.GONE
                        device4ProgressBar.visibility = View.GONE
                        device5ProgressBar.visibility = View.GONE
                        device6ProgressBar.visibility = View.GONE
                        device7ProgressBar.visibility = View.GONE
                        device8ProgressBar.visibility = View.GONE
                        device9ProgressBar.visibility = View.GONE
                        device10ProgressBar.visibility = View.GONE
                    }

                    if (toDateWattage >= 70) {
                        device1ProgressBar.visibility = View.VISIBLE
                        device1Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (toDateWattage >= 200) {
                        device2ProgressBar.visibility = View.VISIBLE
                        device2Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (toDateWattage >= 600) {
                        device3ProgressBar.visibility = View.VISIBLE
                        device3Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (toDateWattage >= 1000) {
                        device4ProgressBar.visibility = View.VISIBLE
                        device4Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (toDateWattage >= 1500) {
                        device5ProgressBar.visibility = View.VISIBLE
                        device5Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (toDateWattage >= 3000) {
                        device6ProgressBar.visibility = View.VISIBLE
                        device6Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (toDateWattage >= 4100) {
                        device7ProgressBar.visibility = View.VISIBLE
                        device7Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (toDateWattage >= 7200) {
                        device8ProgressBar.visibility = View.VISIBLE
                        device8Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (toDateWattage >= 30000) {
                        device9ProgressBar.visibility = View.VISIBLE
                        device9Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
                    }

                    if (toDateWattage >= 100000) {
                        device10ProgressBar.visibility = View.VISIBLE
                        device10Attach.setBackgroundResource(R.drawable.milestones_progress_bar_attach)
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
            powerBarChart?.data = data
            powerBarChart?.axisRight?.isEnabled = false
            powerBarChart?.description?.isEnabled = false

            powerBarChart?.setScaleEnabled(false)
            powerBarChart?.isDoubleTapToZoomEnabled = false

            powerBarChart?.xAxis?.valueFormatter = MyXAxisFormatter()
            powerBarChart?.axisLeft?.axisMinimum = 0f
            powerBarChart?.invalidate()
        }
    }




    class MyXAxisFormatter : ValueFormatter() {
        private val days = arrayOf("", "Sun", "Mon", "Tue", "Wed", "Thr", "Fri", "Sat")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()) ?: value.toString()
        }
    }

    private fun getEntries() {
        barEntries.add(BarEntry(1f, sp.getInt("TotalLog_sunday_log", 0).toFloat()))
        barEntries.add(BarEntry(2f, sp.getInt("TotalLog_monday_log", 0).toFloat()))
        barEntries.add(BarEntry(3f, sp.getInt("TotalLog_tuesday_log", 0).toFloat()))
        barEntries.add(BarEntry(4f, sp.getInt("TotalLog_wednesday_log", 0).toFloat()))
        barEntries.add(BarEntry(5f, sp.getInt("TotalLog_thursday_log", 0).toFloat()))
        barEntries.add(BarEntry(6f, sp.getInt("TotalLog_friday_log", 0).toFloat()))
        barEntries.add(BarEntry(7f, sp.getInt("TotalLog_saturday_log", 0).toFloat()))
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
         * @return A new instance of fragment HistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): HistoryFragment {
            val fragment = HistoryFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
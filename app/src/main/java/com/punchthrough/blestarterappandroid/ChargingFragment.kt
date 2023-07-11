package com.punchthrough.blestarterappandroid

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.lang.Exception
import java.lang.StringBuilder

/**
 * A simple [Fragment] subclass.
 * Use the [ChargingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChargingFragment : Fragment() {
    private var phoneBatteryCapacity = 1

    private var leftShoeProgressBar: ProgressBar? = null
    private var rightShoeProgressBar: ProgressBar? = null

    private var leftShoeProgressText: TextView? = null
    private var rightShoeProgressText: TextView? = null

    private var leftShoeProgress = 0
    private var rightShoeProgress = 0

    private var leftShoeAmpsText: TextView? = null
    private var rightShoeAmpsText: TextView? = null

    private var leftShoeAmps = 0
    private var rightShoeAmps = 0

    private var totalPhoneChargeText: TextView? = null
    private var totalChargeLeftShoeText: TextView? = null
    private var totalChargeRightShoeText: TextView? = null

    private var totalPhoneCharge = 0
    private var totalChargeLeftShoe = 0
    private var totalChargeRightShoe = 0

    private val shoeStatusHandler = Handler(Looper.getMainLooper())

    private var leftShoeBattery : Int? = null
    private var rightShoeBattery: Int? = null

    private val leftShoeBatteryCapacity = 1000
    private val rightShoeBatteryCapacity = 1000

    private fun updateProgress() {
        leftShoeProgressBar!!.progress = leftShoeProgress
        rightShoeProgressBar!!.progress = rightShoeProgress
        leftShoeProgressText!!.text =
            StringBuilder().append(leftShoeProgress).append("%").toString()
        rightShoeProgressText!!.text =
            StringBuilder().append(rightShoeProgress).append("%").toString()
        leftShoeAmpsText!!.text = StringBuilder().append(leftShoeAmps).append("mAh").toString()
        rightShoeAmpsText!!.text = StringBuilder().append(rightShoeAmps).append("mAh").toString()
        totalPhoneChargeText!!.text =
            StringBuilder().append(totalPhoneCharge).append("%").toString()
        totalChargeLeftShoeText!!.text =
            StringBuilder().append(totalChargeLeftShoe).append("%").toString()
        totalChargeRightShoeText!!.text =
            StringBuilder().append(totalChargeRightShoe).append("%").toString()
    }

    private fun getBatteryCapacity(context: Context?): Double {
        val mPowerProfile: Any
        var batteryCapacity = 0.0
        val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                .getConstructor(Context::class.java)
                .newInstance(context)
            batteryCapacity = Class
                .forName(POWER_PROFILE_CLASS)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return batteryCapacity
    }

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
        return inflater.inflate(R.layout.fragment_charging, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        phoneBatteryCapacity = getBatteryCapacity(context).toInt()

        leftShoeProgressBar = requireView().findViewById(R.id.LeftShoeProgressBar)
        rightShoeProgressBar = requireView().findViewById(R.id.RightShoeProgressBar)

        leftShoeProgressText = requireView().findViewById(R.id.LeftProgressTextView)
        rightShoeProgressText = requireView().findViewById(R.id.RightProgressTextView)

        leftShoeAmpsText = requireView().findViewById(R.id.LeftShoeAmpsValueTextView)
        rightShoeAmpsText = requireView().findViewById(R.id.RightShoeAmpsValueTextView)

        totalPhoneChargeText = requireView().findViewById(R.id.TotalPhoneChargeValueTextView)
        totalChargeLeftShoeText = requireView().findViewById(R.id.ChargeFromLeftShoeValueTextView)
        totalChargeRightShoeText = requireView().findViewById(R.id.ChargeFromRightShoeValueTextView)

        Thread {
            while (leftShoeProgress < 110) {
                val mMainActivity1 = activity as MainActivity?
                if (mMainActivity1 != null) {
                    leftShoeBattery = mMainActivity1.getLeftShoeBattery()
                    rightShoeBattery = mMainActivity1.getRightShoeBattery()
                }

                if (leftShoeBattery != null) {
                    leftShoeProgress = leftShoeBattery!! * 100 / leftShoeBatteryCapacity
                    leftShoeAmps = leftShoeBattery!!
                }

                if (rightShoeBattery != null) {
                    rightShoeProgress = rightShoeBattery!! * 100 / rightShoeBatteryCapacity
                    rightShoeAmps = rightShoeBattery!!
                }

                if (leftShoeBattery != null && rightShoeBattery != null) {
                    totalPhoneCharge = (leftShoeAmps + rightShoeAmps) * 100 / phoneBatteryCapacity
                    totalChargeLeftShoe = leftShoeAmps * 100 / phoneBatteryCapacity
                    totalChargeRightShoe = rightShoeAmps * 100 / phoneBatteryCapacity
                }
                shoeStatusHandler.post { updateProgress() }
                SystemClock.sleep(100)
            }
        }.start()
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
         * @return A new instance of fragment ChargingFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): ChargingFragment {
            val fragment = ChargingFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}
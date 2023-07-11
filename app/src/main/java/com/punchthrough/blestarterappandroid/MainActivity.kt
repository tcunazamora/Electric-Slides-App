package com.punchthrough.blestarterappandroid

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.punchthrough.blestarterappandroid.ble.Connect
import com.punchthrough.blestarterappandroid.ble.ConnectionEventListener
import com.punchthrough.blestarterappandroid.ble.ConnectionManager
import com.punchthrough.blestarterappandroid.databinding.ActivityMainBinding
import java.lang.IllegalStateException
import org.jetbrains.anko.alert
import java.lang.StringBuilder
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var sp : SharedPreferences

    private val LEFT_SHOE_DEVICE_NAME = 101
    private val RIGHT_SHOE_DEVICE_NAME = 102

    private lateinit var leftShoeDevice: BluetoothDevice
    private lateinit var rightShoeDevice: BluetoothDevice

    private var isLeftShoeConfigured = false
    private var isRightShoeConfigured = false

    private var leftShoeBattery = -1
    private var rightShoeBattery = -1

    private var currentDay = 0
    private var leftShoeDay = 0
    private var rightShoeDay = 0


    private lateinit var leftShoeWattageLog : ShoeWattageLog
    private lateinit var rightShoeWattageLog : ShoeWattageLog
    private lateinit var totalWattageLog :ShoeWattageLog

    private var dateSentToLeft = false
    private var dateSentToRight = false

    private var currentHour = 0
    private var leftShoeHour = 0
    private var rightShoeHour = 0

    private var hourSentToLeft = false
    private var hourSentToRight = false


    private val leftShoeCharacteristics by lazy {
        ConnectionManager.servicesOnDevice(leftShoeDevice)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }

    private val rightShoeCharacteristics by lazy {
        ConnectionManager.servicesOnDevice(rightShoeDevice)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }


    var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sp = getSharedPreferences("AppData", Context.MODE_PRIVATE)

        leftShoeWattageLog = ShoeWattageLog(sp, "LeftShoeLog")
        rightShoeWattageLog = ShoeWattageLog(sp, "RightShoeLog")
        totalWattageLog = ShoeWattageLog(sp, "TotalLog")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        replaceFragment(ChargingFragment())
        binding!!.bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.charging -> replaceFragment(ChargingFragment())
                R.id.distance -> replaceFragment(DistanceFragment())
                R.id.history -> replaceFragment(HistoryFragment())
            }
            true
        }

        val calendar = Calendar.getInstance();

        Thread {
            if (sp.getInt("current_day", 0) == 0) {
                currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                sp.edit().putInt("current_day", currentDay).commit()
            } else {
                currentDay = sp.getInt("current_day", 0)
            }

            if (sp.getInt("current_day_left", 0) == 0) {
                leftShoeDay = currentDay
                sp.edit().putInt("current_day_left", leftShoeDay).commit()
            } else {
                leftShoeDay = sp.getInt("current_day_left", 0)
            }

            if (sp.getInt("current_day_right", 0) == 0) {
                rightShoeDay = currentDay
                sp.edit().putInt("current_day_right", rightShoeDay).commit()
            } else {
                rightShoeDay = sp.getInt("current_day_right", 0)
            }

            if (sp.getBoolean("date_sent_left", false)) {
                dateSentToLeft = true
            }

            if (sp.getBoolean("date_sent_right", false)) {
                dateSentToRight = true
            }


            if (sp.getInt("current_hour", 0) == 0) {
                currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                sp.edit().putInt("current_hour", currentHour).commit()
            } else {
                currentHour = sp.getInt("current_hour", 0)
            }

            if (sp.getInt("current_hour_left", 0) == 0) {
                leftShoeHour = currentHour
                sp.edit().putInt("current_hour_left", leftShoeHour).commit()
            } else {
                leftShoeHour = sp.getInt("current_hour_left", 0)
            }

            if (sp.getInt("current_hour_right", 0) == 0) {
                rightShoeHour = currentHour
                sp.edit().putInt("current_hour_right", rightShoeDay).commit()
            } else {
                rightShoeHour = sp.getInt("current_hour_right", 0)
            }

            if (sp.getBoolean("hour_sent_left", false)) {
                hourSentToLeft = true
            }

            if (sp.getBoolean("hour_sent_right", false)) {
                hourSentToRight = true
            }

            while(true) {

                if (isLeftShoeConfigured) {
                    var size = leftShoeCharacteristics.size
                    ConnectionManager.registerListener(leftShoeConnectionEventListener)
                    ConnectionManager.readCharacteristic(leftShoeDevice, leftShoeCharacteristics[size - 1])

                    if (!dateSentToLeft) {
                        dateSentToLeft = true
                        sp.edit().putBoolean("date_sent_left", true).apply()
                        ConnectionManager.writeCharacteristic(leftShoeDevice, leftShoeCharacteristics[size - 2], hexToBytes(currentDay.toString(16)))
                        leftShoeDay = currentDay
                        sp.edit().putInt("current_day_left", leftShoeDay).apply()
                    }

                    if (leftShoeDay != calendar.get(Calendar.DAY_OF_WEEK) && dateSentToLeft) {
                        currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                        sp.edit().putInt("current_day", currentDay).commit()
                        leftShoeDay = currentDay
                        sp.edit().putInt("current_day_left", leftShoeDay).commit()
                        leftShoeWattageLog.resetDailyLog()
                        totalWattageLog.resetDailyLog()
                        ConnectionManager.writeCharacteristic(leftShoeDevice, leftShoeCharacteristics[size - 2], hexToBytes(currentDay.toString(16)))
                        SystemClock.sleep(2000)
                        ConnectionManager.readCharacteristic(leftShoeDevice, leftShoeCharacteristics[size - 3])
                    }

                    if (!hourSentToLeft) {
                        hourSentToLeft = true
                        sp.edit().putBoolean("hour_sent_left", true).apply()
                        ConnectionManager.writeCharacteristic(leftShoeDevice, leftShoeCharacteristics[size - 4], hexToBytes(currentHour.toString(16)))
                        leftShoeHour = currentHour
                        sp.edit().putInt("current_hour_left", leftShoeHour).apply()
                    }

                    if (leftShoeHour != calendar.get(Calendar.HOUR_OF_DAY) && hourSentToLeft) {
                        currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                        sp.edit().putInt("current_hour", currentHour).commit()
                        leftShoeHour = currentHour
                        sp.edit().putInt("current_hour_left", leftShoeHour).commit()
                        ConnectionManager.writeCharacteristic(leftShoeDevice, leftShoeCharacteristics[size - 4], hexToBytes(currentHour.toString(16)))
                        SystemClock.sleep(2000)
                        ConnectionManager.readCharacteristic(leftShoeDevice, leftShoeCharacteristics[size - 5])
                        SystemClock.sleep(500)
                        totalWattageLog.addToDailyLog(leftShoeWattageLog.getDailyLog())
                        totalWattageLog.addToDateLog(leftShoeWattageLog.getToDateLog())
                        leftShoeWattageLog.resetDailyLog()
                        leftShoeWattageLog.resetToDateLog()
                    }
                }

                if(isRightShoeConfigured) {
                    var size = rightShoeCharacteristics.size
                    ConnectionManager.registerListener(rightShoeConnectionEventListener)
                    ConnectionManager.readCharacteristic(rightShoeDevice, rightShoeCharacteristics[size - 1])

                    if (!dateSentToRight) {
                        dateSentToRight = true
                        sp.edit().putBoolean("date_sent_right", true).apply()
                        ConnectionManager.writeCharacteristic(rightShoeDevice, rightShoeCharacteristics[size - 2], hexToBytes(currentDay.toString(16)))
                    }

                    if(rightShoeDay != calendar.get(Calendar.DAY_OF_WEEK) && dateSentToRight) {
                        currentDay = calendar.get(Calendar.DAY_OF_WEEK)
                        sp.edit().putInt("current_day", currentDay).commit()
                        rightShoeDay = currentDay
                        sp.edit().putInt("current_day_right", rightShoeDay).apply()
                        rightShoeWattageLog.resetDailyLog()
                        totalWattageLog.resetDailyLog()
                        ConnectionManager.writeCharacteristic(rightShoeDevice, rightShoeCharacteristics[size - 2], hexToBytes(currentDay.toString(16)))
                        SystemClock.sleep(2000)
                        ConnectionManager.readCharacteristic(rightShoeDevice, rightShoeCharacteristics[size - 3])
                    }

                    if (!hourSentToRight) {
                        hourSentToRight = true
                        sp.edit().putBoolean("hour_sent_right", true).apply()
                        ConnectionManager.writeCharacteristic(rightShoeDevice, rightShoeCharacteristics[size - 4], hexToBytes(currentHour.toString(16)))
                        rightShoeHour = currentHour
                        sp.edit().putInt("current_hour_right", rightShoeHour).apply()
                    }

                    if (rightShoeHour != calendar.get(Calendar.HOUR_OF_DAY) && hourSentToRight) {
                        currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                        sp.edit().putInt("current_hour", currentHour).commit()
                        rightShoeHour = currentHour
                        sp.edit().putInt("current_hour_right", rightShoeHour).commit()
                        ConnectionManager.writeCharacteristic(rightShoeDevice, rightShoeCharacteristics[size - 4], hexToBytes(currentHour.toString(16)))
                        SystemClock.sleep(2000)
                        ConnectionManager.readCharacteristic(rightShoeDevice, rightShoeCharacteristics[size - 5])
                        SystemClock.sleep(500)
                        totalWattageLog.addToDailyLog(rightShoeWattageLog.getDailyLog())
                        totalWattageLog.addToDateLog(rightShoeWattageLog.getToDateLog())
                        rightShoeWattageLog.resetDailyLog()
                        rightShoeWattageLog.resetToDateLog()
                    }
                }

                totalWattageLog.writeToCurrentDay(specialDay(currentDay),leftShoeWattageLog.getCurrentDayLog(specialDay(currentDay)) + rightShoeWattageLog.getCurrentDayLog(specialDay(currentDay)))
                totalWattageLog.writeToCurrentHour(specialHour(currentHour), leftShoeWattageLog.getCurrentHourLog(specialHour(currentHour)) + rightShoeWattageLog.getCurrentHourLog(specialHour(currentHour)))
                Log.e("Total Daily Log", totalWattageLog.getDailyLog().toString())
                SystemClock.sleep(3000)
            }
        }.start()

    }

    class ShoeWattageLog(private val sp: SharedPreferences, private val name: String) {
        private var toDateWattageLog = sp.getInt("${name}_to_date_log", 0)
        private var dailyWattageLog = sp.getInt("${name}_daily_log", 0)

        private var sundayLog = sp.getInt("${name}_sunday_log", 0)
        private var mondayLog = sp.getInt("${name}_monday_log", 0)
        private var tuesdayLog = sp.getInt("${name}_tuesday_log", 0)
        private var wednesdayLog = sp.getInt("${name}_wednesday_log", 0)
        private var thursdayLog = sp.getInt("${name}_thursday_log", 0)
        private var fridayLog = sp.getInt("${name}_friday_log", 0)
        private var saturdayLog = sp.getInt("${name}_saturday_log", 0)

        private var hourOneLog = sp.getInt("${name}_hour1_log", 0)
        private var hourTwoLog = sp.getInt("${name}_hour2_log", 0)
        private var hourThreeLog = sp.getInt("${name}_hour3_log", 0)
        private var hourFourLog = sp.getInt("${name}_hour4_log", 0)
        private var hourFiveLog = sp.getInt("${name}_hour5_log", 0)
        private var hourSixLog = sp.getInt("${name}_hour6_log", 0)
        private var hourSevenLog = sp.getInt("${name}_hour7_log", 0)
        private var hourEightLog = sp.getInt("${name}_hour8_log", 0)
        private var hourNineLog = sp.getInt("${name}_hour9_log", 0)
        private var hourTenLog = sp.getInt("${name}_hour10_log", 0)
        private var hourElevenLog = sp.getInt("${name}_hour11_log", 0)
        private var hourTwelveLog = sp.getInt("${name}_hour12_log", 0)
        private var hourThirteenLog = sp.getInt("${name}_hour13_log", 0)
        private var hourFourteenLog = sp.getInt("${name}_hour14_log", 0)
        private var hourFifteenLog = sp.getInt("${name}_hour15_log", 0)
        private var hourSixteenLog = sp.getInt("${name}_hour16_log", 0)
        private var hourSeventeenLog = sp.getInt("${name}_hour17_log", 0)
        private var hourEighteenLog = sp.getInt("${name}_hour18_log", 0)
        private var hourNineteenLog = sp.getInt("${name}_hour19_log", 0)
        private var hourTwentyLog = sp.getInt("${name}_hour20_log", 0)
        private var hourTwentyOneLog = sp.getInt("${name}_hour21_log", 0)
        private var hourTwentyTwoLog = sp.getInt("${name}_hour22_log", 0)
        private var hourTwentyThreeLog = sp.getInt("${name}_hour23_log", 0)
        private var hourTwentyFourLog = sp.getInt("${name}_hour24_log", 0)

        fun resetToDateLog() {
            this.toDateWattageLog = 0
            sp.edit().putInt("${name}_to_date_log", this.toDateWattageLog).apply()
        }

        fun resetDailyLog() {
            this.dailyWattageLog = 0
            sp.edit().putInt("${name}_daily_log", this.dailyWattageLog).apply()
        }

        fun addToDateLog(value: Int) {
            this.toDateWattageLog += value
            sp.edit().putInt("${name}_to_date_log", this.toDateWattageLog).apply()
        }

        fun addToDailyLog(value: Int) {
            this.dailyWattageLog += value
            sp.edit().putInt("${name}_daily_log", this.dailyWattageLog).apply()
        }

        fun getToDateLog() : Int {
            return this.toDateWattageLog
        }

        fun getDailyLog() : Int {
            return this.dailyWattageLog
        }

        fun writeToCurrentHour(day: Int, value: Int) {
            when (day) {
                1 -> {
                    this.hourOneLog = value
                    sp.edit().putInt("${name}_hour1_log", this.hourOneLog).apply()
                }
                2 -> {
                    this.hourTwoLog = value
                    sp.edit().putInt("${name}_hour2_log", this.hourTwoLog).apply()

                }
                3 -> {
                    this.hourThreeLog = value
                    sp.edit().putInt("${name}_hour3_log", this.hourThreeLog).apply()

                }
                4 -> {
                    this.hourFourLog = value
                    sp.edit().putInt("${name}_hour4_log", this.hourFourLog).apply()

                }
                5 -> {
                    this.hourFiveLog = value
                    sp.edit().putInt("${name}_hour5_log", this.hourFiveLog).apply()

                }
                6 -> {
                    this.hourSixLog = value
                    sp.edit().putInt("${name}_hour6_log", this.hourSixLog).apply()

                }
                7 -> {
                    this.hourSevenLog = value
                    sp.edit().putInt("${name}_hour7_log", this.hourSevenLog).apply()

                }
                8 -> {
                    this.hourEightLog = value
                    sp.edit().putInt("${name}_hour8_log", this.hourEightLog).apply()

                }
                9 -> {
                    this.hourNineLog = value
                    sp.edit().putInt("${name}_hour9_log", this.hourNineLog).apply()

                }
                10 -> {
                    this.hourTenLog = value
                    sp.edit().putInt("${name}_hour10_log", this.hourTenLog).apply()

                }
                11 -> {
                    this.hourElevenLog = value
                    sp.edit().putInt("${name}_hour11_log", this.hourElevenLog).apply()

                }
                12 -> {
                    this.hourTwelveLog = value
                    sp.edit().putInt("${name}_hour12_log", this.hourTwelveLog).apply()

                }
                13 -> {
                    this.hourThirteenLog = value
                    sp.edit().putInt("${name}_hour13_log", this.hourThirteenLog).apply()

                }
                14 -> {
                    this.hourFourteenLog = value
                    sp.edit().putInt("${name}_hour14_log", this.hourFourteenLog).apply()

                }
                15 -> {
                    this.hourFifteenLog = value
                    sp.edit().putInt("${name}_hour15_log", this.hourFifteenLog).apply()

                }
                16 -> {
                    this.hourSixteenLog = value
                    sp.edit().putInt("${name}_hour16_log", this.hourSixteenLog).apply()

                }
                17 -> {
                    this.hourSeventeenLog = value
                    sp.edit().putInt("${name}_hour17_log", this.hourSeventeenLog).apply()

                }
                18 -> {
                    this.hourEighteenLog = value
                    sp.edit().putInt("${name}_hour18_log", this.hourEighteenLog).apply()

                }
                19 -> {
                    this.hourNineteenLog = value
                    sp.edit().putInt("${name}_hour19_log", this.hourNineteenLog).apply()

                }
                20 -> {
                    this.hourTwentyLog = value
                    sp.edit().putInt("${name}_hour20_log", this.hourTwentyLog).apply()

                }
                21 -> {
                    this.hourTwentyOneLog = value
                    sp.edit().putInt("${name}_hour21_log", this.hourTwentyOneLog).apply()

                }
                22 -> {
                    this.hourTwentyTwoLog = value
                    sp.edit().putInt("${name}_hour22_log", this.hourTwentyTwoLog).apply()

                }
                23 -> {
                    this.hourTwentyThreeLog = value
                    sp.edit().putInt("${name}_hour23_log", this.hourTwentyThreeLog).apply()

                }
                24 -> {
                    this.hourTwentyFourLog = value
                    sp.edit().putInt("${name}_hour24_log", this.hourTwentyFourLog).apply()

                }
            }
        }


        fun getCurrentHourLog(day: Int) : Int {
            return when (day) {
                1 -> this.hourOneLog
                2 -> this.hourTwoLog
                3 -> this.hourThreeLog
                4 -> this.hourFourLog
                5 -> this.hourFiveLog
                6 -> this.hourSixLog
                7 -> this.hourSevenLog
                8 -> this.hourEightLog
                9 -> this.hourNineLog
                10 -> this.hourTenLog
                11 -> this.hourElevenLog
                12 -> this.hourTwelveLog
                13 -> this.hourThirteenLog
                14 -> this.hourFourteenLog
                15 -> this.hourFifteenLog
                16 -> this.hourSixteenLog
                17 -> this.hourSeventeenLog
                18 -> this.hourEighteenLog
                19 -> this.hourNineteenLog
                20 -> this.hourTwentyLog
                21 -> this.hourTwentyOneLog
                22 -> this.hourTwentyTwoLog
                23 -> this.hourTwentyThreeLog
                0 -> this.hourTwentyFourLog
                else -> 0
            }
        }

        fun writeToCurrentDay(day: Int, value: Int) {
            when (day) {
                1 -> {
                    this.sundayLog = value
                    sp.edit().putInt("${name}_sunday_log", this.sundayLog).apply()
                }
                2 -> {
                    this.mondayLog = value
                    sp.edit().putInt("${name}_monday_log", this.mondayLog).apply()
                }
                3 -> {
                    this.tuesdayLog = value
                    sp.edit().putInt("${name}_tuesday_log", this.tuesdayLog).apply()
                }
                4 -> {
                    this.wednesdayLog = value
                    sp.edit().putInt("${name}_wednesday_log", this.wednesdayLog).apply()
                }
                5 -> {
                    this.thursdayLog = value
                    sp.edit().putInt("${name}_thursday_log", this.thursdayLog).apply()
                }
                6 -> {
                    this.fridayLog = value
                    sp.edit().putInt("${name}_friday_log", this.fridayLog).apply()
                }
                7 -> {
                    this.saturdayLog = value
                    sp.edit().putInt("${name}_saturday_log", this.saturdayLog).apply()
                }
            }
        }

        fun getCurrentDayLog(day: Int) : Int {
            return when (day) {
                1 -> this.sundayLog
                2 -> this.mondayLog
                3 -> this.tuesdayLog
                4 -> this.wednesdayLog
                5 -> this.thursdayLog
                6 -> this.fridayLog
                7 -> this.saturdayLog
                else -> 0
            }
        }
    }

    public fun getLeftShoeBattery(): Int {
        return leftShoeBattery
    }

    public fun getRightShoeBattery(): Int {
        return rightShoeBattery
    }


    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_shoe_selection, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.config_left_shoe -> {
                val intent = Intent(this, ScanActivity::class.java)
                startActivityForResult(intent, LEFT_SHOE_DEVICE_NAME)
            }
            R.id.config_right_shoe ->{
                val intent = Intent(this, ScanActivity::class.java)
                startActivityForResult(intent, RIGHT_SHOE_DEVICE_NAME)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LEFT_SHOE_DEVICE_NAME && resultCode == RESULT_OK) {
            try {
                Log.e("Activity Result", "Trying to get Device")
                if (data != null) {
                    leftShoeDevice = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    isLeftShoeConfigured = true
                    Toast.makeText(this, "Left Shoe Configured", Toast.LENGTH_SHORT).show()
                    Log.e("Activity Result", "Left Device Received")
                }

            } catch (e: IllegalStateException) {
                Log.e("Activity Result", "getExtra was null")
            }
        }

        if (requestCode == RIGHT_SHOE_DEVICE_NAME && resultCode == RESULT_OK) {
            try {
                Log.e("Activity Result", "Trying to get Device")
                if (data != null) {
                    rightShoeDevice = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    isRightShoeConfigured = true
                    Toast.makeText(this, "Right Shoe Configured", Toast.LENGTH_SHORT).show()
                    Log.e("Activity Result", "Right Device Received")
                }
            } catch (e: IllegalStateException) {
                Log.e("Activity Result", "getExtra was null")
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private val leftShoeConnectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                runOnUiThread {
                    alert {
                        title = "Disconnected"
                        message = "Disconnected from device."
                        positiveButton("OK") { onBackPressed() }
                    }.show()
                }
            }

            onCharacteristicRead = { _, characteristic ->
                var size = leftShoeCharacteristics.size
                if (characteristic == leftShoeCharacteristics[size - 1]) {
                    leftShoeBattery = Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() }))
                }

                if (characteristic == leftShoeCharacteristics[size - 3]) {
                    var log = Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() }))
                    Log.e("Left Shoe Daily Log", log.toString())
                    if (Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })) != 0) {
                        leftShoeWattageLog.writeToCurrentDay(specialDay(currentDay), Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })))
                    }
                }

                if (characteristic == leftShoeCharacteristics[size - 5]) {
                    var log = Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() }))
                    Log.e("Left Shoe Hourly Log", log.toString())
                    if (Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })) != 0) {
                        leftShoeWattageLog.writeToCurrentHour(specialHour(currentHour), Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })))
                        leftShoeWattageLog.addToDailyLog(Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })))
                        leftShoeWattageLog.addToDateLog(Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })))
                        Log.e("Left Shoe Daily Log", leftShoeWattageLog.getDailyLog().toString())
                    }
                }
            }

            onCharacteristicWrite = { _, characteristic ->
                Log.e("Char Write","Wrote to ${characteristic.uuid}")
            }
        }
    }

    private val rightShoeConnectionEventListener by lazy {
        ConnectionEventListener().apply {
            onDisconnect = {
                runOnUiThread {
                    alert {
                        title = "Disconnected"
                        message = "Disconnected from device."
                        positiveButton("OK") { onBackPressed() }
                    }.show()
                }
            }

            onCharacteristicRead = { _, characteristic ->
                var size = rightShoeCharacteristics.size
                if (characteristic == rightShoeCharacteristics[size - 1]) {
                    rightShoeBattery = Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() }))

                }

                if(characteristic == rightShoeCharacteristics[size - 3]) {
                    if (Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })) != 0) {
                        rightShoeWattageLog.writeToCurrentDay(specialDay(currentDay), Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })))
                    }
                }

                if (characteristic == rightShoeCharacteristics[size - 5]) {
                    if (Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })) != 0) {
                        rightShoeWattageLog.writeToCurrentHour(specialHour(currentHour), Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })))
                        rightShoeWattageLog.addToDailyLog(Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })))
                        rightShoeWattageLog.addToDateLog(Integer.decode(removeEmptyBytes(characteristic.value.toHexString().filter { !it.isWhitespace() })))
                    }
                }
            }
        }
    }

    private fun removeEmptyBytes(str : String): String {
        Log.e("Hex String", str)
        var newStr = ""
        if (str[4].toString() == "0" && str[5].toString() == "0") {
            newStr = str.slice(0..3)
            return newStr
        }
        val byte1 = str.slice(2..3)
        val byte2 = str.slice(4..5)
        newStr = StringBuilder("0X").append(byte2).append(byte1).toString()

        return newStr
    }

    private fun hexToBytes(str: String) : ByteArray {
        return str.chunked(2).map { it.toUpperCase(Locale.US).toInt(16).toByte() }.toByteArray()
    }

    private fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

    private fun specialDay(day: Int) : Int {
        if (day == 1) {
            return 7
        }
        return day - 1
    }

    private fun specialHour(hour : Int) : Int {
        if (hour == 1) {
            return 24
        }
        return hour - 1
    }
}

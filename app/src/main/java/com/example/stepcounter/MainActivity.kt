package com.example.stepcounter

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvStepCount: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView

    private val REQUEST_ACTIVITY_RECOGNITION = 1001
    private lateinit var sharedPref: SharedPreferences


    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


    private val stepReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val newSteps = intent?.getIntExtra("step_count", loadStepCount()) ?: loadStepCount()
            saveStepData(newSteps)
            updateUI(newSteps)

            checkForNewDayAndReset()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        sharedPref = getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE)


        tvStepCount = findViewById(R.id.tvStepCount)
        tvDistance = findViewById(R.id.tvDistance)
        tvCalories = findViewById(R.id.tvCalories)


        registerReceiver(stepReceiver, IntentFilter("STEP_COUNT_UPDATE"), Context.RECEIVER_NOT_EXPORTED)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_ACTIVITY_RECOGNITION
            )
        } else {
            startStepCountingService()
        }


        updateUI(loadStepCount())
        checkForNewDayAndReset()


        findViewById<Button>(R.id.btnStatistics).setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
        findViewById<Button>(R.id.btnMain).setOnClickListener {

        }
        findViewById<Button>(R.id.btnMaps).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }

    private fun startStepCountingService() {
        startService(Intent(this, StepCountingService::class.java))
    }


    private fun updateUI(steps: Int) {
        val stepLengthMeters = 0.7
        val caloriesPerStep = 0.04

        val distanceKm = steps * stepLengthMeters / 1000
        val caloriesBurned = steps * caloriesPerStep

        tvStepCount.text = "Пройдено шагов: $steps"
        tvDistance.text = "Пройдено км: %.2f".format(distanceKm)
        tvCalories.text = "Сожжено калорий: %.2f".format(caloriesBurned)
    }

    private fun saveStepData(steps: Int) {
        with(sharedPref.edit()) {
            putInt("today_steps", steps)
            putString("last_date", dateFormat.format(Date()))
            apply()
        }
    }


    private fun loadStepCount(): Int {
        return sharedPref.getInt("today_steps", 0)
    }


    private fun checkForNewDayAndReset() {
        val lastDate = sharedPref.getString("last_date", null)
        val currentDate = dateFormat.format(Date())

        if (lastDate != null && lastDate != currentDate) {
            moveDataToStatistics()
            resetDailyData()
        }
    }

    private fun moveDataToStatistics() {
        val steps = loadStepCount()
        val stepLengthMeters = 0.7
        val caloriesPerStep = 0.04

        val distanceKm = steps * stepLengthMeters / 1000
        val caloriesBurned = steps * caloriesPerStep

        val statisticsPref = getSharedPreferences("StatisticsPrefs", Context.MODE_PRIVATE)
        val existingData = statisticsPref.getString("data", "") ?: ""
        val currentDate = dateFormat.format(Date())


        val newEntry = "$currentDate,$steps,${"%.2f".format(distanceKm)},${"%.2f".format(caloriesBurned)}"
        val updatedData = if (existingData.isEmpty()) newEntry else "$existingData\n$newEntry"

        with(statisticsPref.edit()) {
            putString("data", updatedData)
            apply()
        }
    }


    private fun resetDailyData() {
        with(sharedPref.edit()) {
            putInt("today_steps", 0)
            putString("last_date", dateFormat.format(Date()))
            apply()
        }
        updateUI(0)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStepCountingService()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(stepReceiver)
    }
}

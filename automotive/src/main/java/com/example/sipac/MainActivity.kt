package com.example.sipac

import android.Manifest
import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        textView = findViewById(R.id.textView)

        requestPermissions()
        initializeCar()
    }

    override fun onResume() {
        super.onResume()
        listenSensorData()
    }

    override fun onPause() {
        if (car.isConnected) {
            car.disconnect()
        }

        super.onPause()
    }

    private val permissions = arrayOf(
        Car.PERMISSION_SPEED,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun requestPermissions() {
        val requestMultiplePermission =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    Log.e("DEBUG", "${it.key} = ${it.value}")
                }
            }

        requestMultiplePermission.launch(permissions)
    }

    private lateinit var car: Car
    private lateinit var carPropertyManager: CarPropertyManager

    private fun initializeCar() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            return
        }

        if (::car.isInitialized) {
            return
        }

        car = Car.createCar(this)
        carPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
    }

    private fun listenSensorData() {
        val speedCallback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
                val speed = ((carPropertyValue.value as Float)*3.6).toInt()
                val text = "$speed km/h"
                textView.text = text
                println(speed)
            }

            override fun onErrorEvent(p0: Int, p1: Int) {
                println("Error retrieving sensor data")
            }
        }

        carPropertyManager.registerCallback(
            speedCallback,
            VehiclePropertyIds.PERF_VEHICLE_SPEED,
            CarPropertyManager.SENSOR_RATE_NORMAL
        )
    }
}
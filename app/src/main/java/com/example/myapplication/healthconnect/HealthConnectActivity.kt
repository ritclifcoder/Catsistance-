package com.example.myapplication.healthconnect

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.healthconnect.HealthConnectManager

class HealthConnectActivity : AppCompatActivity() {
    
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) {
            Toast.makeText(this, "Health Connect permissions granted!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val healthConnectManager = HealthConnectManager.getInstance(this)
        
        if (!healthConnectManager.isAvailable) {
            Toast.makeText(this, "Health Connect not available", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        requestPermissions.launch(HealthConnectManager.PERMISSIONS.toTypedArray())
    }
}

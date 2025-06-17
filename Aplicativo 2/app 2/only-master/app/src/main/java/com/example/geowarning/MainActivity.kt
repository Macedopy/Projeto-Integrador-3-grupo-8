package com.example.geowarning

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MenuRiscos::class.java)
        startActivity(intent)

        finish()
    }
}


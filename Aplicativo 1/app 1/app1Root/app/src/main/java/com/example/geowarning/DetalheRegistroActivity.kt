package com.example.geowarning

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import java.io.File
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DetalheRegistroActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_registro)

        // Get data from Intent
        latitude = intent.getDoubleExtra("lat", 0.0)
        longitude = intent.getDoubleExtra("lng", 0.0)
        val title = intent.getStringExtra("title") ?: "Sem título"
        val description = intent.getStringExtra("description") ?: "Sem descrição"
        val riskLevel = intent.getStringExtra("riskLevel") ?: "Desconhecido"
        val imageTempPath = intent.getStringExtra("imageTempPath")
        val imageBase64: String? = try {
            imageTempPath?.let { path -> File(path).readText() }
        } catch (e: Exception) {
            Log.e("DetalheRegistro", "Erro ao ler o arquivo temporário da imagem", e)
            null
        }

        Log.d("DetalheRegistro", "Received coordinates: lat=$latitude, lng=$longitude")

        // Set up UI elements
        val textDescription = findViewById<TextView>(R.id.textDescription)
        val textRiskLevel = findViewById<TextView>(R.id.textRiskLevel)
        val imageView = findViewById<ImageView>(R.id.imageView)

        textDescription.text = description
        textRiskLevel.text = riskLevel

        // Display the image
        if (imageBase64 != null) {
            try {
                val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Imagem não disponível.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Imagem não disponível.", Toast.LENGTH_SHORT).show()
        }

        // Set up back button
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Return to the previous activity
        }

        // Request location permissions before setting up the map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            setupMap()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap()
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        } else {
            Toast.makeText(this, "Erro ao carregar o mapa.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val incidentLocation = LatLng(latitude, longitude)

        // Check for invalid coordinates
        val finalLocation = if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Coordenadas inválidas, usando localização padrão.", Toast.LENGTH_SHORT).show()
            LatLng(-23.5505, -46.6333) // São Paulo, Brazil
        } else {
            incidentLocation
        }

        mMap.addMarker(MarkerOptions().position(finalLocation).title("Local do Incidente"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(finalLocation, 15f))

        // Enable My Location layer if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
    }
}
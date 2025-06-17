package com.example.geowarning

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.geowarning.Location.LocationData
import com.example.geowarning.network.ApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class CadastroLocalizacaoActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    private var selectedImageBase64: String? = null
    private var selectedRiskLevel: String? = null

    companion object {
        const val IMAGE_PICK_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_localizacao)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnEnviar = findViewById<Button>(R.id.btnEnviarLocalizacao)
        btnEnviar.setOnClickListener {
            if (currentLatitude != null && currentLongitude != null) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, IMAGE_PICK_CODE)
            } else {
                Toast.makeText(this, "Localização ainda não disponível", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                selectedImageBase64 = convertBitmapToBase64(bitmap)
                // Switch to the details layout
                showDetailsPage()
            }
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun showDetailsPage() {
        setContentView(R.layout.activity_cadastro_detalhes)

        val btnEstavel = findViewById<Button>(R.id.btnEstavel)
        val btnModerado = findViewById<Button>(R.id.btnModerado)
        val btnPerigoso = findViewById<Button>(R.id.btnPerigoso)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val editTextTitulo = findViewById<EditText>(R.id.editTextTitulo)
        val editTextDescricao = findViewById<EditText>(R.id.editTextDescricao)

        btnEstavel.setOnClickListener {
            selectedRiskLevel = "Estável"
            btnEstavel.isSelected = true
            btnModerado.isSelected = false
            btnPerigoso.isSelected = false
        }

        btnModerado.setOnClickListener {
            selectedRiskLevel = "Moderado"
            btnEstavel.isSelected = false
            btnModerado.isSelected = true
            btnPerigoso.isSelected = false
        }

        btnPerigoso.setOnClickListener {
            selectedRiskLevel = "Perigoso"
            btnEstavel.isSelected = false
            btnModerado.isSelected = false
            btnPerigoso.isSelected = true
        }

        btnConfirmar.setOnClickListener {
            val title = editTextTitulo.text.toString().trim()
            val description = editTextDescricao.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Por favor, insira o título", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                Toast.makeText(this, "Por favor, insira a descrição", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedRiskLevel == null) {
                Toast.makeText(this, "Por favor, selecione o nível de risco", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            enviarLocalizacao(
                latitude = currentLatitude!!,
                longitude = currentLongitude!!,
                imageBase64 = selectedImageBase64!!,
                title = title,
                description = description,
                riskLevel = selectedRiskLevel!!
            )
        }

        btnCancelar.setOnClickListener {
            // Return to the first page
            selectedImageBase64 = null
            selectedRiskLevel = null
            setContentView(R.layout.activity_cadastro_localizacao)
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }

    private fun enviarLocalizacao(
        latitude: Double,
        longitude: Double,
        imageBase64: String,
        title: String,
        description: String,
        riskLevel: String
    ) {
        val locationData = LocationData(
            latitude = latitude,
            longitude = longitude,
            userId = "123",
            imageBase64 = imageBase64,
            title = title,
            description = description,
            riskLevel = riskLevel
        )

        ApiClient.retrofit.sendLocation(locationData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CadastroLocalizacaoActivity, "Localização e dados enviados com sucesso!", Toast.LENGTH_LONG).show()
                    finish() // Close the Activity after successful submission
                } else {
                    Toast.makeText(this@CadastroLocalizacaoActivity, "Erro ao enviar: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@CadastroLocalizacaoActivity, "Erro ao enviar: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                val userLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                mMap.addMarker(MarkerOptions().position(userLatLng).title("Minha Localização"))
            }
        }
    }
}
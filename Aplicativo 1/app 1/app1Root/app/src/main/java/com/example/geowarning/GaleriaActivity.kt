package com.example.geowarning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geowarning.network.ApiService
import com.example.geowarning.Registro.Registro
import com.example.geowarning.Registro.RegistroAdapter
import com.example.geowarning.Location.LocationData
import com.example.geowarning.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class GaleriaActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RegistroAdapter
    private lateinit var registros: List<Registro>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galeria)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Initialize adapter with empty list
        registros = emptyList()
        adapter = RegistroAdapter(registros) { registro ->
            redirectToDetails(registro)
        }
        recyclerView.adapter = adapter

        // Fetch locations from API
        fetchLocations()

        val btnCadastrar = findViewById<Button>(R.id.btnCadastrarLocal)
        btnCadastrar.setOnClickListener {
            val intent = Intent(this, CadastroLocalizacaoActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchLocations() // Refresh data when resuming
    }

    // Function to handle redirection with the selected item's data
    private fun redirectToDetails(registro: Registro) {
        val tempFile = File.createTempFile("image", ".txt", cacheDir)
        tempFile.writeText(registro.imageBase64 ?: "")
        val intent = Intent(this, DetalheRegistroActivity::class.java)
        intent.putExtra("lat", registro.latitude)
        intent.putExtra("lng", registro.longitude)
        intent.putExtra("title", registro.title ?: "Sem título")
        intent.putExtra("description", registro.description ?: "Sem descrição")
        intent.putExtra("riskLevel", registro.riskLevel ?: "Desconhecido")
        intent.putExtra("imageTempPath", tempFile.absolutePath)
        startActivity(intent)
    }

    private fun fetchLocations() {
        ApiClient.retrofit.getLocations().enqueue(object : Callback<List<LocationData>> {
            override fun onResponse(call: Call<List<LocationData>>, response: Response<List<LocationData>>) {
                if (response.isSuccessful) {
                    response.body()?.let { locations ->
                        registros = locations.map { location ->
                            Registro(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                title = location.title ?: "Sem título",
                                description = location.description ?: "Sem descrição",
                                riskLevel = location.riskLevel ?: "Desconhecido",
                                imageBase64 = location.imageBase64,
                                userId = "555"
                            )
                        }
                        runOnUiThread {
                            adapter.updateData(registros)
                        }                    } ?: run {
                        Toast.makeText(this@GaleriaActivity, "No data received", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@GaleriaActivity, "Failed to fetch data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<LocationData>>, t: Throwable) {
                Toast.makeText(this@GaleriaActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
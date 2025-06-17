package com.example.geowarning

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.geowarning.Registro.Registro
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalheRegistroActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    // Lista de registros a serem exibidos no mapa, vinda da Intent
    private var registrosToDisplay: List<Registro> = emptyList()
    private var selectedRiskLevel: String? = null

    private lateinit var textViewReportTitles: TextView
    private lateinit var textViewReportDescriptions: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_registro)

        // Recebe os dados da Intent
        registrosToDisplay = intent.getParcelableArrayListExtra<Registro>("registrosList") ?: emptyList()
        selectedRiskLevel = intent.getStringExtra("selectedRiskLevel")

        Log.d("DetalheRegistroActivity", "onCreate: Recebeu ${registrosToDisplay.size} registros para o nível de risco: $selectedRiskLevel")

        // Log detalhado dos registros recebidos para depuração
        if (registrosToDisplay.isEmpty()) {
            Log.w("DetalheRegistroActivity", "onCreate: Nenhum registro recebido na Intent. O mapa pode ficar vazio.")
        } else {
            registrosToDisplay.forEachIndexed { index, registro ->
                Log.d("DetalheRegistroActivity", "  Registro $index: Título='${registro.title}', Lat=${registro.latitude}, Lng=${registro.longitude}, Risco='${registro.riskLevel}', Timestamp='${registro.timestamp}'")
            }
        }
        imageView = findViewById(R.id.imageView)

        textViewReportTitles = findViewById(R.id.textViewReportTitles)
        textViewReportDescriptions = findViewById(R.id.textViewReportDescriptions)

        // Atualiza a UI textual com base nos registros recebidos
        if (registrosToDisplay.isNotEmpty()) {
            val reportBuilder = StringBuilder()

            reportBuilder.append("Total de Registros: ${registrosToDisplay.size}\n")
            reportBuilder.append("Nível de Risco: ${selectedRiskLevel ?: "Desconhecido"}\n\n")

            reportBuilder.append("Locais Encontrados:\n")
            registrosToDisplay.forEachIndexed { index, registro ->
                reportBuilder.append("  ${index + 1}. ${registro.title ?: "Sem título"}\n")
            }
            textViewReportTitles.text = reportBuilder.toString()

            // REMOVED: Code block for textViewReportDates
            // val sortedRegistros = registrosToDisplay.sortedBy { it.timestamp }
            // if (sortedRegistros.isNotEmpty()) {
            //     val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            //     val firstTimestamp = sortedRegistros.first().timestamp
            //     val lastTimestamp = sortedRegistros.last().timestamp
            //
            //     val firstDate: Date? = try { sdf.parse(firstTimestamp) } catch (e: Exception) {
            //         Log.e("DetalheRegistroActivity", "Erro ao parsear data inicial: ${firstTimestamp}", e)
            //         null
            //     }
            //     val lastDate: Date? = try { sdf.parse(lastTimestamp) } catch (e: Exception) {
            //         Log.e("DetalheRegistroActivity", "Erro ao parsear data final: ${lastTimestamp}", e)
            //         null
            //     }
            //
            //     textViewReportDates.text = "Período dos Registros:\n" +
            //             "  Início: ${if (firstDate != null) sdf.format(firstDate) else "N/A"}\n" +
            //             "  Fim: ${if (lastDate != null) sdf.format(lastDate) else "N/A"}"
            // } else {
            //     textViewReportDates.text = "Período dos Registros: N/A"
            // }

            val descriptionsBuilder = StringBuilder()
            descriptionsBuilder.append("Descrições dos Registros:\n")
            registrosToDisplay.forEachIndexed { index, registro ->
                val description = registro.description?.takeIf { it.isNotBlank() } ?: "Sem descrição"
                descriptionsBuilder.append("  ${index + 1}. ${registro.title ?: "Local ${index+1}"}: $description\n")
            }
            textViewReportDescriptions.text = descriptionsBuilder.toString()

            imageView.visibility = ImageView.GONE // Imagem não é usada aqui

        } else {
            // Caso não haja registros para exibir
            imageView.visibility = ImageView.GONE
            textViewReportTitles.text = ""
            // REMOVED: textViewReportDates.text = ""
            textViewReportDescriptions.text = ""
            Toast.makeText(this, "Nenhum registro para exibir neste detalhe.", Toast.LENGTH_SHORT).show()
            Log.i("DetalheRegistroActivity", "UI textual: Nenhum registro para exibir.")
        }

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Retorna à atividade anterior
        }

        // Solicita permissão de localização se ainda não concedida
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Se a permissão já foi concedida, configura o mapa
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
                Log.d("DetalheRegistroActivity", "Permissão de localização concedida. Configurando mapa.")
                setupMap()
            } else {
                Toast.makeText(this, "Permissão de localização negada. O mapa pode não funcionar corretamente.", Toast.LENGTH_LONG).show()
                Log.w("DetalheRegistroActivity", "Permissão de localização negada pelo usuário.")
            }
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
            Log.d("DetalheRegistroActivity", "setupMap: Chamando getMapAsync.")
        } else {
            Toast.makeText(this, "Erro ao carregar o mapa.", Toast.LENGTH_SHORT).show()
            Log.e("DetalheRegistroActivity", "Erro: mapFragment é nulo. Fragmento do mapa não encontrado no layout 'activity_detalhe_registro.xml'.")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.clear() // Limpa quaisquer marcadores ou sobreposições anteriores

        Log.d("DetalheRegistroActivity", "onMapReady: GoogleMap está pronto. Limpando mapa e ajustando câmera.")

        mMap.setOnMapLoadedCallback {
            Log.d("DetalheRegistroActivity", "onMapReady: Google Map carregado com sucesso (tiles carregados).")
            adjustMapCamera()
        }

        mMap.setOnMapClickListener {
            Log.w("DetalheRegistroActivity", "onMapReady: Clique no mapa detectado. Se o mapa estiver cinza, pode indicar problema de carregamento de tiles.")
        }

        adjustMapCamera()
    }

    private fun adjustMapCamera() {
        Log.d("DetalheRegistroActivity", "adjustMapCamera: Iniciando ajuste da câmera. Registros para exibir: ${registrosToDisplay.size}")

        if (registrosToDisplay.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            var validMarkersAdded = 0

            registrosToDisplay.forEach { registro ->
                // Verifica se as coordenadas são válidas antes de adicionar o marcador
                if (registro.latitude != 0.0 || registro.longitude != 0.0) {
                    val location = LatLng(registro.latitude, registro.longitude)
                    mMap.addMarker(MarkerOptions().position(location).title(registro.title))
                    boundsBuilder.include(location)
                    validMarkersAdded++
                    Log.d("DetalheRegistroActivity", "  Marcador adicionado para: '${registro.title}' em Lat=${registro.latitude}, Lng=${registro.longitude}")
                } else {
                    Log.w("DetalheRegistroActivity", "  Registro com coordenadas inválidas (0.0, 0.0) ou nulas: '${registro.title}'. Marcador não adicionado.")
                }
            }

            if (validMarkersAdded > 0) {
                try {
                    val bounds = boundsBuilder.build()
                    val padding = 100 // Preenchimento em pixels nas bordas do mapa
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                    Log.d("DetalheRegistroActivity", "Câmera ajustada para Bounds com $validMarkersAdded marcadores válidos.")
                } catch (e: IllegalStateException) {
                    // Isso pode acontecer se 'boundsBuilder' não tiver nenhum ponto incluído,
                    // embora 'validMarkersAdded > 0' já deva prevenir isso.
                    Log.e("DetalheRegistroActivity", "Erro ao construir LatLngBounds (provavelmente sem pontos válidos): ${e.message}", e)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.55052, -46.633308), 10f)) // Ex: São Paulo, Brasil
                    Toast.makeText(this, "Não foi possível centralizar o mapa nos pontos. Exibindo localização padrão.", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.w("DetalheRegistroActivity", "Nenhum marcador válido encontrado nos registros. Exibindo localização padrão.")
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.55052, -46.633308), 10f)) // Ex: São Paulo, Brasil
                Toast.makeText(this, "Nenhum registro com coordenadas válidas para exibir no mapa. Exibindo localização padrão.", Toast.LENGTH_LONG).show()
            }
        } else {
            // Caso 'registrosToDisplay' esteja vazio desde o início
            Log.i("DetalheRegistroActivity", "registrosToDisplay está vazio. Nenhum dado para exibir no mapa. Exibindo localização padrão.")
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.55052, -46.633308), 10f)) // Ex: São Paulo, Brasil
            Toast.makeText(this, "Nenhum registro para exibir no mapa. Exibindo localização padrão.", Toast.LENGTH_SHORT).show()
        }
    }

    // Esta função foi mantida, mas em um ambiente de produção você enviaria para um serviço de monitoramento.
    private fun sendErrorLogToDeveloper(errorCode: String, exception: Throwable?) {
        val errorMessage = "Erro no Mapa (DetalheRegistroActivity): $errorCode" +
                (exception?.let { " - ${it.javaClass.simpleName}: ${it.message}" } ?: "")
        Log.e("APP_ERROR_REPORT", errorMessage, exception) // Log com tag específica para erros

        // Apenas para feedback visual rápido durante o desenvolvimento
        Toast.makeText(this, "Um erro foi detectado e registrado no Logcat. Por favor, verifique.", Toast.LENGTH_LONG).show()
    }
}
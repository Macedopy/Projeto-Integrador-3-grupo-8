package com.example.geowarning

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.geowarning.Registro.Registro
import com.example.geowarning.Location.LocationData
import com.example.geowarning.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuRiscos : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var textViewStartDate: TextView
    private lateinit var textViewEndDate: TextView
    private var allRegistros: List<Registro> = emptyList()

    // Esta lista armazena os registros filtrados por data
    private var currentFilteredRegistrosByDate: List<Registro> = emptyList()

    private lateinit var textViewPerigosoCount: TextView
    private lateinit var textViewModeradoCount: TextView
    private lateinit var textViewEstavelCount: TextView

    private lateinit var cardPerigoso: CardView
    private lateinit var cardModerado: CardView
    private lateinit var cardEstavel: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galeria)

        textViewStartDate = findViewById(R.id.textViewStartDate)
        textViewEndDate = findViewById(R.id.textViewEndDate)
        val btnApplyFilter = findViewById<Button>(R.id.btnApplyFilter)

        textViewPerigosoCount = findViewById(R.id.textViewPerigosoCount)
        textViewModeradoCount = findViewById(R.id.textViewModeradoCount)
        textViewEstavelCount = findViewById(R.id.textViewEstavelCount)

        cardPerigoso = findViewById(R.id.cardPerigoso)
        cardModerado = findViewById(R.id.cardModerado)
        cardEstavel = findViewById(R.id.cardEstavel)

        // Listeners para os CardViews que levarão ao mapa
        cardPerigoso.setOnClickListener {
            redirectToMapDetails("Perigoso")
        }
        cardModerado.setOnClickListener {
            redirectToMapDetails("Moderado")
        }
        cardEstavel.setOnClickListener {
            redirectToMapDetails("Estável")
        }

        textViewStartDate.setOnClickListener {
            showDatePicker(textViewStartDate)
        }

        textViewEndDate.setOnClickListener {
            showDatePicker(textViewEndDate)
        }

        btnApplyFilter.setOnClickListener {
            applyDateFilter()
        }

        // Carrega as localizações na inicialização
        fetchLocations()
    }

    override fun onResume() {
        super.onResume()
        // Limpa os campos de data e recarrega os dados ao retornar para a Galeria
        textViewStartDate.text = ""
        textViewEndDate.text = ""
        fetchLocations()
    }

    private fun showDatePicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1, // Mês é baseado em 0
                    selectedDay
                )
                textView.text = formattedDate
                Toast.makeText(this, "Selecionado: $formattedDate", Toast.LENGTH_SHORT).show()
            },
            year,
            month,
            day
        )
        // Impede a seleção de datas futuras
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun applyDateFilter() {
        val startDateStr = textViewStartDate.text.toString()
        val endDateStr = textViewEndDate.text.toString()

        // Se as datas não foram selecionadas, assume todos os registros
        if (startDateStr.isEmpty() || startDateStr == textViewStartDate.hint.toString() ||
            endDateStr.isEmpty() || endDateStr == textViewEndDate.hint.toString()) {
            Toast.makeText(this, "Por favor, selecione as datas de início e fim. Exibindo todos os registros.", Toast.LENGTH_LONG).show()
            currentFilteredRegistrosByDate = allRegistros
            updateRiskCounts(allRegistros)
            Log.d("MenuRiscos", "Filtro de data não aplicado: Exibindo todos os ${allRegistros.size} registros.")
            return
        }

        launch {
            try {
                val filteredRegistros = withContext(Dispatchers.Default) {
                    val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    sdfInput.isLenient = false // Garante parsing estrito

                    val startDate = sdfInput.parse(startDateStr)
                    val endDate = sdfInput.parse(endDateStr)

                    if (startDate == null || endDate == null || startDate.after(endDate)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MenuRiscos, "Intervalo de datas inválido. Verifique as datas.", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("MenuRiscos", "applyDateFilter: Intervalo de datas inválido detectado.")
                        return@withContext emptyList<Registro>()
                    }

                    // Ajusta a data final para incluir o dia inteiro
                    val calendarEndDate = Calendar.getInstance()
                    calendarEndDate.time = endDate
                    calendarEndDate.set(Calendar.HOUR_OF_DAY, 23)
                    calendarEndDate.set(Calendar.MINUTE, 59)
                    calendarEndDate.set(Calendar.SECOND, 59)
                    calendarEndDate.set(Calendar.MILLISECOND, 999)
                    val adjustedEndDate = calendarEndDate.time

                    Log.d("MenuRiscos", "Data Inicial Selecionada para filtro: $startDateStr -> $startDate")
                    Log.d("MenuRiscos", "Data Final Ajustada para filtro (fim do dia): $endDateStr -> $adjustedEndDate")

                    val sdfDatabase = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                    sdfDatabase.isLenient = false

                    allRegistros.filter { registro ->
                        val registroDate = try {
                            sdfDatabase.parse(registro.timestamp ?: run {
                                Log.w("MenuRiscos", "Registro com timestamp nulo ou vazio ignorado no filtro de data: ${registro.title}")
                                return@filter false
                            })
                        } catch (e: Exception) {
                            Log.e("MenuRiscos", "Erro ao parsear data '${registro.timestamp}' do registro '${registro.title}': ${e.message}", e)
                            return@filter false
                        }

                        // Inclui o registro se a data estiver dentro do intervalo [startDate, adjustedEndDate]
                        registroDate != null &&
                                !registroDate.before(startDate) &&
                                !registroDate.after(adjustedEndDate)
                    }
                }

                currentFilteredRegistrosByDate = filteredRegistros
                updateRiskCounts(currentFilteredRegistrosByDate)

                Toast.makeText(
                    this@MenuRiscos,
                    if (currentFilteredRegistrosByDate.isEmpty()) "Nenhum registro encontrado para o intervalo selecionado." else "Contagens atualizadas para ${currentFilteredRegistrosByDate.size} registros.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("MenuRiscos", "Filtro de data aplicado. Registros filtrados: ${currentFilteredRegistrosByDate.size}.")

            } catch (e: Exception) {
                Toast.makeText(this@MenuRiscos, "Erro inesperado ao aplicar filtro: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("MenuRiscos", "Erro geral no applyDateFilter: ${e.message}", e)
            }
        }
    }

    private fun redirectToMapDetails(riskLevel: String) {
        // Filtra os registros que já foram filtrados por data, agora por nível de risco
        val filteredForMap = currentFilteredRegistrosByDate.filter {
            if (riskLevel == "Desconhecido") {
                // Considera nulo ou vazio como "Desconhecido"
                it.riskLevel == "Desconhecido" || it.riskLevel.isNullOrBlank()
            } else {
                it.riskLevel == riskLevel
            }
        }

        Log.d("MenuRiscos", "redirectToMapDetails: Nível de Risco Clicado: '$riskLevel'")
        Log.d("MenuRiscos", "redirectToMapDetails: Registros filtrados por data: ${currentFilteredRegistrosByDate.size}")
        Log.d("MenuRiscos", "redirectToMapDetails: Registros filtrados por nível de risco (antes de verificar coordenadas): ${filteredForMap.size}")

        if (filteredForMap.isEmpty()) {
            Toast.makeText(this, "Nenhum registro '$riskLevel' encontrado no intervalo selecionado para exibir no mapa.", Toast.LENGTH_SHORT).show()
            Log.w("MenuRiscos", "Nenhum registro '$riskLevel' após filtro de risco.")
            return
        }

        // Mapeia para uma nova lista de Registro, ignorando a imagem e filtrando coordenadas inválidas
        val registrosValidosParaMapa = filteredForMap.mapNotNull { registro ->
            if (registro.latitude != 0.0 || registro.longitude != 0.0) {
                Registro(
                    latitude = registro.latitude,
                    longitude = registro.longitude,
                    title = registro.title,
                    description = registro.description,
                    riskLevel = registro.riskLevel,
                    imageBase64 = null, // Não precisamos da imagem para o mapa
                    userId = registro.userId,
                    timestamp = registro.timestamp
                )
            } else {
                Log.w("MenuRiscos", "Registro com coordenadas inválidas (0.0, 0.0) ou nulas ignorado para o mapa: '${registro.title}'. Lat: ${registro.latitude}, Lng: ${registro.longitude}")
                null // Retorna null para descartar este registro
            }
        }

        Log.d("MenuRiscos", "redirectToMapDetails: Registros válidos para o mapa (após verificar coordenadas): ${registrosValidosParaMapa.size}")

        if (registrosValidosParaMapa.isEmpty()) {
            Toast.makeText(this, "Nenhum registro '$riskLevel' com coordenadas válidas para exibir no mapa.", Toast.LENGTH_SHORT).show()
            Log.w("MenuRiscos", "Nenhum registro '$riskLevel' com coordenadas válidas.")
            return
        }

        // Prepara a Intent com a lista de registros e o nível de risco selecionado
        val intent = Intent(this, DetalheRegistroActivity::class.java).apply {
            putParcelableArrayListExtra("registrosList", ArrayList(registrosValidosParaMapa))
            putExtra("selectedRiskLevel", riskLevel)
        }
        startActivity(intent)
    }

    private fun updateRiskCounts(listToCount: List<Registro>) {
        val perigosoCount = listToCount.count { it.riskLevel == "Perigoso" }
        val moderadoCount = listToCount.count { it.riskLevel == "Moderado" }
        val estavelCount = listToCount.count { it.riskLevel == "Estável" || it.riskLevel.isNullOrBlank() }

        runOnUiThread {
            textViewPerigosoCount.text = perigosoCount.toString()
            textViewModeradoCount.text = moderadoCount.toString()
            textViewEstavelCount.text = estavelCount.toString()
        }
        Log.d("MenuRiscos", "Contagens atualizadas: Perigoso=$perigosoCount, Moderado=$moderadoCount, Desconhecido=$estavelCount")
    }

    private fun fetchLocations() {
        ApiClient.retrofit.getLocations().enqueue(object : Callback<List<LocationData>> {
            override fun onResponse(call: Call<List<LocationData>>, response: Response<List<LocationData>>) {
                if (response.isSuccessful) {
                    response.body()?.let { locations ->
                        launch {
                            val mappedRegistros = withContext(Dispatchers.Default) {
                                locations.mapNotNull { location ->
                                    // Log de cada localização recebida da API
                                    // CUIDADO: Este log pode ser muito verboso com muitos dados
                                    // Log.d("MenuRiscos", "API Location: Title=${location.title}, Lat=${location.latitude}, Lng=${location.longitude}, Risk=${location.riskLevel}, Timestamp=${location.timestamp}")

                                    if (location.latitude != null && location.longitude != null &&
                                        (location.latitude != 0.0 || location.longitude != 0.0)
                                    ) {
                                        Registro(
                                            latitude = location.latitude,
                                            longitude = location.longitude,
                                            title = location.title ?: "Sem título",
                                            description = location.description ?: "Sem descrição",
                                            riskLevel = location.riskLevel ?: "Desconhecido",
                                            imageBase64 = null, // Não precisamos da imagem neste contexto
                                            userId = "555", // Considere passar o userId real se disponível
                                            timestamp = location.timestamp
                                        )
                                    } else {
                                        Log.w("MenuRiscos", "Registro ignorado da API devido a coordenadas inválidas/nulas: '${location.title}'. Lat: ${location.latitude}, Lng: ${location.longitude}")
                                        null // Retorna null para descartar registros com coordenadas inválidas
                                    }
                                }
                            }
                            allRegistros = mappedRegistros
                            currentFilteredRegistrosByDate = allRegistros // Define o filtro inicial para todos os registros
                            updateRiskCounts(allRegistros)
                            Log.d("MenuRiscos", "Total de registros carregados na inicialização (válidos): ${allRegistros.size}")
                        }
                    } ?: run {
                        Toast.makeText(this@MenuRiscos, "Nenhum dado recebido da API.", Toast.LENGTH_SHORT).show()
                        Log.w("MenuRiscos", "Corpo da resposta da API é nulo.")
                        updateRiskCounts(emptyList())
                        currentFilteredRegistrosByDate = emptyList()
                    }
                } else {
                    Toast.makeText(this@MenuRiscos, "Falha ao buscar dados: Código ${response.code()}", Toast.LENGTH_LONG).show()
                    Log.e("MenuRiscos", "Falha na resposta da API: ${response.code()} - ${response.message()}")
                    updateRiskCounts(emptyList())
                    currentFilteredRegistrosByDate = emptyList()
                }
            }

            override fun onFailure(call: Call<List<LocationData>>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MenuRiscos, "Erro de rede: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("MenuRiscos", "Falha na requisição da API: ${t.message}", t)
                updateRiskCounts(emptyList())
                currentFilteredRegistrosByDate = emptyList()
            }
        })
    }
}
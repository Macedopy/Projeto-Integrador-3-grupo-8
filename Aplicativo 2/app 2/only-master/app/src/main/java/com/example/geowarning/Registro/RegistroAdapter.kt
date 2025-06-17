package com.example.geowarning.Registro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geowarning.R

class RegistroAdapter(
    private var registros: List<Registro>,
    private val onItemClick: (Registro) -> Unit
) : RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder>() {

    class RegistroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registro, parent, false)
        return RegistroViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegistroViewHolder, position: Int) {
        val registro = registros[position]
        holder.textView.text = registro.description ?: registro.title ?: "Sem descrição"

        // *** MUDANÇAS AQUI: REMOVA O CÓDIGO DE imageBase64 ***
        // Em vez de carregar a imagem do Base64, você pode:
        // 1. Ocultar a ImageView se não houver imagem:
        holder.imageView.visibility = View.GONE

        // OU 2. Definir uma imagem padrão com base no nível de risco:
        /*
        when (registro.riskLevel) {
            "Perigoso" -> holder.imageView.setImageResource(R.drawable.ic_risk_perigoso) // Crie esses drawables
            "Moderado" -> holder.imageView.setImageResource(R.drawable.ic_risk_moderado)
            else -> holder.imageView.setImageResource(R.drawable.ic_risk_desconhecido) // Ou um ícone genérico
        }
        holder.imageView.visibility = View.VISIBLE // Certifique-se de que está visível
        */

        // Remova ou comente todo este bloco, pois registro.imageBase64 não existe mais:
        /*
        if (registro.imageBase64 != null) {
            try {
                val decodedBytes = Base64.decode(registro.imageBase64, Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // Lidar com erro de decodificação, talvez mostrar uma imagem padrão
                holder.imageView.setImageResource(R.drawable.ic_default_error_image) // Crie este drawable
            }
        } else {
            holder.imageView.setImageResource(R.drawable.ic_default_no_image) // Crie este drawable
        }
        */

        // Make the ImageView clickable
        holder.imageView.isClickable = true
        holder.imageView.isFocusable = true
        holder.imageView.setOnClickListener {
            onItemClick(registro)
        }
    }

    override fun getItemCount(): Int = registros.size

    fun updateData(newRegistros: List<Registro>) {
        registros = newRegistros
        notifyDataSetChanged()
    }
}
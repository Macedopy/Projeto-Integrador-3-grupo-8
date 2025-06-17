package com.example.geowarning.Registro

import android.util.Base64
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
        if (registro.imageBase64 != null) {
            try {
                val decodedBytes = Base64.decode(registro.imageBase64, Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
            }
        }
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
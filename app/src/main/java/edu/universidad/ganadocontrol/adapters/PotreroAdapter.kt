package edu.universidad.ganadocontrol.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.universidad.ganadocontrol.R
import edu.universidad.ganadocontrol.database.Potrero
import java.io.File

class PotreroAdapter(
    private val potreros: List<Potrero>,
    private val onItemClick: (Potrero) -> Unit,
    private val onItemLongClick: (Potrero) -> Boolean
) : RecyclerView.Adapter<PotreroAdapter.PotreroViewHolder>() {

    class PotreroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPreview: ImageView = view.findViewById(R.id.ivPotreroPreview)
        val tvName: TextView = view.findViewById(R.id.tvPotreroName)
        val tvArea: TextView = view.findViewById(R.id.tvPotreroArea)
        val tvDate: TextView = view.findViewById(R.id.tvPotreroDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PotreroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_potrero, parent, false)
        return PotreroViewHolder(view)
    }

    override fun onBindViewHolder(holder: PotreroViewHolder, position: Int) {
        val potrero = potreros[position]
        holder.tvName.text = potrero.nombre
        holder.tvArea.text = "Medida: ${potrero.medidas} mts²"
        holder.tvDate.text = "Creado: ${potrero.fechaCreacion}"

        // Cargar foto si existe
        if (!potrero.fotoPath.isNullOrEmpty()) {
            val file = File(potrero.fotoPath)
            if (file.exists()) {
                try {
                    // Decodificar la imagen a escala para evitar consumo excesivo de memoria (OOM)
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 4 // escala 1/4
                    }
                    val bitmap = BitmapFactory.decodeFile(potrero.fotoPath, options)
                    if (bitmap != null) {
                        holder.ivPreview.setImageBitmap(bitmap)
                    } else {
                        holder.ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                } catch (e: Exception) {
                    holder.ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                holder.ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onItemClick(potrero)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(potrero)
        }
    }

    override fun getItemCount(): Int = potreros.size
}

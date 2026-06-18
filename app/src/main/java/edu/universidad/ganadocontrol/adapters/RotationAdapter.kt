package edu.universidad.ganadocontrol.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.universidad.ganadocontrol.R
import edu.universidad.ganadocontrol.database.HistoricoRotacion
import edu.universidad.ganadocontrol.database.Potrero

data class PotreroRotationState(
    val potrero: Potrero,
    val state: String, // "RED", "ORANGE", "GREEN"
    var isChecked: Boolean,
    val activeRotation: HistoricoRotacion?
)

class RotationAdapter(
    private val items: List<PotreroRotationState>,
    private val onItemClick: (PotreroRotationState) -> Unit,
    private val onItemLongClick: (PotreroRotationState) -> Boolean
) : RecyclerView.Adapter<RotationAdapter.RotationViewHolder>() {

    class RotationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewStatusIndicator: View = view.findViewById(R.id.viewStatusIndicator)
        val tvName: TextView = view.findViewById(R.id.tvRotationName)
        val tvStatusDesc: TextView = view.findViewById(R.id.tvRotationStatusDesc)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RotationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rotation, parent, false)
        return RotationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RotationViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.potrero.nombre

        // Determinar colores y descripciones
        val (colorHex, statusText) = when (item.state) {
            "RED" -> Pair("#EF5350", "Estado: Con ganado (pastoreo)")
            "ORANGE" -> Pair("#FFA726", "Estado: En recuperación (descanso)")
            else -> Pair("#66BB6A", "Estado: Recuperado (libre)")
        }

        holder.tvStatusDesc.text = statusText
        holder.tvName.setTextColor(Color.parseColor(colorHex))

        // Fondo del ítem según estado
        val bgColor = when (item.state) {
            "RED" -> Color.parseColor("#FFEBEE")
            "ORANGE" -> Color.parseColor("#FFF3E0")
            else -> Color.parseColor("#E8F5E9")
        }
        holder.itemView.setBackgroundColor(bgColor)

        // Generar círculo de estado coloreado programáticamente
        val circleDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(colorHex))
        }
        holder.viewStatusIndicator.background = circleDrawable

        // Checkbox binding
        holder.cbSelect.isChecked = item.isChecked
        // Ocultar/deshabilitar checkbox si el potrero no está verde
        if (item.state != "GREEN") {
            holder.cbSelect.visibility = View.INVISIBLE
            holder.cbSelect.isEnabled = false
        } else {
            holder.cbSelect.visibility = View.VISIBLE
            holder.cbSelect.isEnabled = true
        }

        holder.itemView.setOnClickListener {
            if (item.state == "GREEN") {
                item.isChecked = !item.isChecked
                holder.cbSelect.isChecked = item.isChecked
                onItemClick(item)
            } else {
                // Si no está libre, al hacer clic podemos notificar o simplemente ignorar
                onItemClick(item)
            }
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}

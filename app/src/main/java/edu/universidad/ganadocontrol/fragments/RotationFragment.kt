package edu.universidad.ganadocontrol.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.universidad.ganadocontrol.R
import edu.universidad.ganadocontrol.adapters.PotreroRotationState
import edu.universidad.ganadocontrol.adapters.RotationAdapter
import edu.universidad.ganadocontrol.database.AppDatabase
import edu.universidad.ganadocontrol.database.HistoricoRotacion
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RotationFragment : Fragment() {

    private lateinit var etRotationDate: EditText
    private lateinit var rvRotation: RecyclerView
    private lateinit var btnLoadCattle: Button
    private lateinit var db: AppDatabase

    private val calendar = Calendar.getInstance()
    private val selectedPotrerosIds = mutableSetOf<Int>()
    private var selectedDateMillis: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rotation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())
        etRotationDate = view.findViewById(R.id.etRotationDate)
        rvRotation = view.findViewById(R.id.rvRotation)
        btnLoadCattle = view.findViewById(R.id.btnLoadCattle)

        rvRotation.layoutManager = LinearLayoutManager(context)

        // Set default date as Today
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etRotationDate.setText(sdf.format(Date()))
        updateSelectedDateMillis()

        etRotationDate.setOnClickListener {
            showDatePicker()
        }

        btnLoadCattle.setOnClickListener {
            loadCattleToSelected()
        }

        loadRotationStatuses()
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) {
            loadRotationStatuses()
        }
    }

    private fun updateSelectedDateMillis() {
        // Normalizar fecha al inicio del día (evitar desfases de horas)
        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.HOUR_OF_DAY, 0)
        tempCal.set(Calendar.MINUTE, 0)
        tempCal.set(Calendar.SECOND, 0)
        tempCal.set(Calendar.MILLISECOND, 0)
        selectedDateMillis = tempCal.timeInMillis
    }

    private fun showDatePicker() {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            etRotationDate.setText(sdf.format(calendar.time))
            updateSelectedDateMillis()
            
            // Al cambiar la fecha, limpiamos la selección y recargamos
            selectedPotrerosIds.clear()
            loadRotationStatuses()
        }

        DatePickerDialog(
            requireContext(),
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadRotationStatuses() {
        val dao = db.ganadoDao()
        val potreros = dao.getAllPotreros()
        val allRotations = dao.getAllRotaciones()

        val items = mutableListOf<PotreroRotationState>()

        for (potrero in potreros) {
            // Buscar si hay una rotación que cubra la fecha seleccionada
            val activeRotation = allRotations.firstOrNull { rot ->
                rot.potreroId == potrero.id && 
                selectedDateMillis >= rot.fechaInicio && 
                selectedDateMillis < rot.fechaFinNaranja
            }

            val state = if (activeRotation != null) {
                if (selectedDateMillis < activeRotation.fechaFinRojo) {
                    "RED"
                } else {
                    "ORANGE"
                }
            } else {
                "GREEN"
            }

            val isChecked = selectedPotrerosIds.contains(potrero.id)
            items.add(PotreroRotationState(potrero, state, isChecked, activeRotation))
        }

        rvRotation.adapter = RotationAdapter(
            items,
            onItemClick = { stateItem ->
                if (stateItem.state == "GREEN") {
                    if (stateItem.isChecked) {
                        selectedPotrerosIds.add(stateItem.potrero.id)
                    } else {
                        selectedPotrerosIds.remove(stateItem.potrero.id)
                    }
                }
            },
            onItemLongClick = { stateItem ->
                showRotationDetailDialog(stateItem)
                true
            }
        )
    }

    private fun loadCattleToSelected() {
        if (selectedPotrerosIds.isEmpty()) {
            Toast.makeText(context, "Por favor, selecciona al menos un potrero libre (Verde)", Toast.LENGTH_SHORT).show()
            return
        }

        val dao = db.ganadoDao()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val fiveDaysMs = 5 * oneDayMs
        val fifteenDaysMs = 15 * oneDayMs

        var count = 0
        val skipped = mutableListOf<String>()

        for (potreroId in selectedPotrerosIds.toList()) {
            val potrero = dao.getPotreroById(potreroId) ?: continue

            // Verificar que el potrero esté verde en la fecha seleccionada
            val existingRotation = dao.getAllRotaciones().firstOrNull { rot ->
                rot.potreroId == potreroId &&
                selectedDateMillis >= rot.fechaInicio &&
                selectedDateMillis < rot.fechaFinNaranja
            }

            if (existingRotation != null) {
                skipped.add(potrero.nombre)
                continue
            }

            val rotation = HistoricoRotacion(
                potreroId,
                selectedDateMillis,
                selectedDateMillis + fiveDaysMs,
                selectedDateMillis + fiveDaysMs + fifteenDaysMs
            )
            dao.insertRotacion(rotation)
            count++
        }

        if (count > 0) {
            Toast.makeText(context, "Se cargó ganado en $count potrero(s)", Toast.LENGTH_SHORT).show()
        }
        if (skipped.isNotEmpty()) {
            Toast.makeText(
                context,
                "No se pudo cargar en: ${skipped.joinToString(", ")} (no están libres)",
                Toast.LENGTH_LONG
            ).show()
        }

        selectedPotrerosIds.clear()
        loadRotationStatuses()
    }

    private fun showRotationDetailDialog(item: PotreroRotationState) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(item.potrero.nombre)

        val allRotations = db.ganadoDao().getRotacionesForPotrero(item.potrero.id)

        if (allRotations.isEmpty() && item.activeRotation == null) {
            builder.setMessage("Estado actual: RECUPERADO (LIBRE) 🟢\n\nEste potrero está disponible para asignación de ganado.")
        } else {
            val message = StringBuilder()

            // Estado en la fecha consultada
            val estadoFecha = when (item.state) {
                "RED" -> "OCUPADO CON GANADO 🔴"
                "ORANGE" -> "EN RECUPERACIÓN 🟠"
                else -> "RECUPERADO (LIBRE) 🟢"
            }
            message.append("Estado en fecha consultada: $estadoFecha\n\n")

            if (item.activeRotation != null) {
                val startStr = sdf.format(Date(item.activeRotation.fechaInicio))
                val finRojoStr = sdf.format(Date(item.activeRotation.fechaFinRojo))
                val finNaranjaStr = sdf.format(Date(item.activeRotation.fechaFinNaranja))

                message.append("Rotación activa en esta fecha:\n")
                message.append("  Período Rojo (pastoreo): $startStr al $finRojoStr\n")
                message.append("  Período Anaranjado (descanso): $finRojoStr al $finNaranjaStr\n")
                message.append("  Liberación (Verde): $finNaranjaStr\n\n")
            }

            if (allRotations.isNotEmpty()) {
                message.append("── Histórico de rotaciones ──\n")
                allRotations.forEachIndexed { index, rot ->
                    val startStr = sdf.format(Date(rot.fechaInicio))
                    val finRojoStr = sdf.format(Date(rot.fechaFinRojo))
                    val finNaranjaStr = sdf.format(Date(rot.fechaFinNaranja))
                    message.append("\n#${index + 1}\n")
                    message.append("  Rojo: $startStr → $finRojoStr\n")
                    message.append("  Anaranjado: $finRojoStr → $finNaranjaStr\n")
                }
            }

            builder.setMessage(message.toString().trim())
        }

        builder.setPositiveButton("Cerrar", null)
        builder.show()
    }
}

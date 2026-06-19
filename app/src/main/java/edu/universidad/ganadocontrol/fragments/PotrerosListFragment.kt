package edu.universidad.ganadocontrol.fragments

import android.os.Bundle
import edu.universidad.ganadocontrol.MainActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.universidad.ganadocontrol.R
import edu.universidad.ganadocontrol.adapters.PotreroAdapter
import edu.universidad.ganadocontrol.database.AppDatabase
import edu.universidad.ganadocontrol.database.Potrero
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PotrerosListFragment : Fragment() {

    private lateinit var rvPotreros: RecyclerView
    private lateinit var fabAddPotrero: FloatingActionButton
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_potreros_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        db = AppDatabase.getDatabase(requireContext())
        rvPotreros = view.findViewById(R.id.rvPotreros)
        fabAddPotrero = view.findViewById(R.id.fabAddPotrero)

        rvPotreros.layoutManager = LinearLayoutManager(context)

        fabAddPotrero.setOnClickListener {
            // Abrir formulario en modo creación (id = 0 o nulo)
            openFormFragment(null)
        }

        loadPotreros()
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) {
            loadPotreros()
        }
    }

    private fun loadPotreros() {
        val dao = db.ganadoDao()
        var potrerosList = dao.getAllPotreros()

        // Si la lista está vacía, registrar 5 potreros por defecto e históricos de prueba
        if (potrerosList.isEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = sdf.format(Date())

            val id1 = dao.insertPotrero(Potrero("Potrero El Establo", 5000.0, currentDate, null, null)).toInt()
            val id2 = dao.insertPotrero(Potrero("Potrero La Colina", 7200.0, currentDate, null, null)).toInt()
            val id3 = dao.insertPotrero(Potrero("Potrero Las Palmas", 6100.0, currentDate, null, null)).toInt()
            val id4 = dao.insertPotrero(Potrero("Potrero Río Claro", 4800.0, currentDate, null, null)).toInt()
            val id5 = dao.insertPotrero(Potrero("Potrero La Vega", 8500.0, currentDate, null, null)).toInt()

            // Generar fechas relativas para los estados
            val oneDayMs = 24 * 60 * 60 * 1000L
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val todayMs = cal.timeInMillis

            // 1. Potrero El Establo: Con ganado (Rojo). Empezó hace 2 días.
            val startRed = todayMs - 2 * oneDayMs
            val rotRed = edu.universidad.ganadocontrol.database.HistoricoRotacion(
                id1,
                startRed,
                startRed + 5 * oneDayMs,
                startRed + 20 * oneDayMs
            )
            dao.insertRotacion(rotRed)

            // 2. Potrero La Colina: En descanso (Anaranjado). Empezó hace 10 días.
            val startOrange = todayMs - 10 * oneDayMs
            val rotOrange = edu.universidad.ganadocontrol.database.HistoricoRotacion(
                id2,
                startOrange,
                startOrange + 5 * oneDayMs,
                startOrange + 20 * oneDayMs
            )
            dao.insertRotacion(rotOrange)

            potrerosList = dao.getAllPotreros()
        }

        rvPotreros.adapter = PotreroAdapter(
            potrerosList,
            onItemClick = { potrero ->
                openFormFragment(potrero.id)
            },
            onItemLongClick = { potrero ->
                showDeleteDialog(potrero)
                true
            }
        )
    }

    private fun openFormFragment(potreroId: Int?) {
        (activity as? MainActivity)?.loadFormWithPotreroId(potreroId)
    }

    private fun showDeleteDialog(potrero: Potrero) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Potrero")
            .setMessage("¿Estás seguro de que deseas eliminar '${potrero.nombre}'? Se borrará todo su historial de rotaciones asociado en cascada.")
            .setPositiveButton("Eliminar") { _, _ ->
                db.ganadoDao().deletePotreroWithHistorico(potrero)
                Toast.makeText(context, "Potrero eliminado", Toast.LENGTH_SHORT).show()
                loadPotreros() // recargar la lista
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

package edu.universidad.ganadocontrol

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.universidad.ganadocontrol.fragments.PotreroFormFragment
import edu.universidad.ganadocontrol.fragments.PotrerosListFragment
import edu.universidad.ganadocontrol.fragments.RotationFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomNavigation = findViewById(R.id.bottom_navigation)
        setupNavigationListener()

        // Carga inicial
        if (savedInstanceState == null) {
            loadFragment(PotrerosListFragment())
        }
    }

    private fun setupNavigationListener() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.navigation_potreros -> PotrerosListFragment()
                R.id.navigation_form -> PotreroFormFragment()
                R.id.navigation_rotation -> RotationFragment()
                else -> null
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment)
                true
            } else {
                false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun loadFormWithPotreroId(potreroId: Int?) {
        // Seleccionar la pestaña de registro visualmente sin disparar el listener recursivo
        bottomNavigation.setOnItemSelectedListener(null)
        bottomNavigation.selectedItemId = R.id.navigation_form
        setupNavigationListener()

        // Cargar el fragmento del formulario con los argumentos
        val formFragment = PotreroFormFragment().apply {
            arguments = Bundle().apply {
                if (potreroId != null) {
                    putInt("potrero_id", potreroId)
                }
            }
        }
        loadFragment(formFragment)
    }

    fun navigateToTab(itemId: Int) {
        bottomNavigation.selectedItemId = itemId
    }

    fun navigateToPotrerosList() {
        bottomNavigation.setOnItemSelectedListener(null)
        bottomNavigation.selectedItemId = R.id.navigation_potreros
        setupNavigationListener()
        loadFragment(PotrerosListFragment())
    }
}
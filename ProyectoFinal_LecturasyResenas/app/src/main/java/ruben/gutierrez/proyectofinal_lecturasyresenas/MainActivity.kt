package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ruben.gutierrez.proyectofinal_lecturasyresenas.model.Libro

class MainActivity : AppCompatActivity() {


    private lateinit var viewModel: LibroViewModel
    private lateinit var adapter: LibroAdapter

    private var filtroActual: String? = null

    private var listaOriginal: List<Libro> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val recycler = findViewById<RecyclerView>(R.id.recyclerViewLibros)
        adapter = LibroAdapter { libro ->
            val intent = Intent(this, DetalleLibroActivity::class.java)
            intent.putExtra("idLibro", libro.id)
            startActivity(intent)
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter



        val tabs = findViewById<TabLayout>(R.id.tabs)

        // creamos los tabs manualmente
            //con "tabs" me refiero a los filtros del menú
        tabs.addTab(tabs.newTab().setText("Todos"))
        tabs.addTab(tabs.newTab().setText("Por leer"))
        tabs.addTab(tabs.newTab().setText("En curso"))
        tabs.addTab(tabs.newTab().setText("Terminados"))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                filtroActual = when (tab?.position) {
                    0 -> null
                    1 -> "Por leer"
                    2 -> "En curso"
                    3 -> "Terminado"
                    else -> null
                }

                Log.d("TAB_DEBUG", "Tab seleccionado: ${tab?.position} | filtro: $filtroActual")

                filtrarLibrosPorEstado(filtroActual)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                onTabSelected(tab)
            }
        })

        //PARA EL FIREBASE
        viewModel = ViewModelProvider(this)[LibroViewModel::class.java]

        viewModel.libros.observe(this) { lista ->
            listaOriginal = lista

            // esto APLICA el filtro actual
            filtrarLibrosPorEstado(filtroActual)
        }


        //ESTO ES LA BARRA INFERIOR
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_biblioteca

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_biblioteca -> true
                R.id.nav_estadisticas -> {
                    startActivity(Intent(this, EstadisticasActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        //FABICON PARA AGREGAR LIBRO
        findViewById<FloatingActionButton>(R.id.fab_agregar_libro).setOnClickListener {
            startActivity(Intent(this, AgregarLibroActivity::class.java))
        }

        //BOTON PARA IR AL PERFIL
        findViewById<ImageView>(R.id.boton_perfil).setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }
    }



    // Esto es para FILTRAR libros
    private fun filtrarLibrosPorEstado(estado: String?) {

        val listaFiltrada = if (estado == null) {
            listaOriginal
        } else {
            listaOriginal.filter { it.estadoLectura == estado }
        }

        Log.d("FILTRADO_DEBUG", "Filtro: $estado | libros: ${listaFiltrada.size}")

        // enviar nueva lista siempre
        adapter.submitList(ArrayList(listaFiltrada))
    }
}


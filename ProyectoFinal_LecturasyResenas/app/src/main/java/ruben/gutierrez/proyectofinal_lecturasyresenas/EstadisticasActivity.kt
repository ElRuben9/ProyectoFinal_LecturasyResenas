package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import ruben.gutierrez.proyectofinal_lecturasyresenas.utilities.CustomBarDrawable
import ruben.gutierrez.proyectofinal_lecturasyresenas.utilities.CustomCircleDrawable
import ruben.gutierrez.proyectofinal_lecturasyresenas.utilities.Category
import ruben.gutierrez.proyectofinal_lecturasyresenas.utilities.FullBarChartDrawable
import ruben.gutierrez.proyectofinal_lecturasyresenas.utilities.FullPieChartDrawable

class EstadisticasActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_estadisticas)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        inicializarTarjetas()
        cargarEstadisticas()

        //Esto es para que el icono de estadísticas esté seleccionado
        bottomNavigation.selectedItemId = R.id.nav_estadisticas

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_biblioteca -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun inicializarTarjetas() {
        findViewById<View>(R.id.cardLibrosAno).findViewById<TextView>(R.id.tvDescripcion).text = "Libros este año"
        findViewById<View>(R.id.cardLibrosMes).findViewById<TextView>(R.id.tvDescripcion).text = "Libros este mes"
        findViewById<View>(R.id.cardPaginasAno).findViewById<TextView>(R.id.tvDescripcion).text = "Páginas este año"
        findViewById<View>(R.id.cardPaginasMes).findViewById<TextView>(R.id.tvDescripcion).text = "Páginas este mes"

        findViewById<View>(R.id.graficaLibrosMes).findViewById<TextView>(R.id.tvTituloGrafica).text = "Libros leídos por mes"
        findViewById<View>(R.id.graficaLibrosAno).findViewById<TextView>(R.id.tvTituloGrafica).text = "Libros leídos por año"
        findViewById<View>(R.id.graficaCategorias).findViewById<TextView>(R.id.tvTituloGrafica).text = "Libros por categoría"
        findViewById<View>(R.id.graficaGeneros).findViewById<TextView>(R.id.tvTituloGrafica).text = "Libros por género"
        findViewById<View>(R.id.graficaPaginasMes).findViewById<TextView>(R.id.tvTituloGrafica).text = "Páginas leídas por mes"
        findViewById<View>(R.id.graficaPaginasAno).findViewById<TextView>(R.id.tvTituloGrafica).text = "Páginas leídas por año"
    }

    private fun cargarEstadisticas() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val firestore = FirebaseFirestore.getInstance()

        val sdfMes = java.text.SimpleDateFormat("yyyy-MM")
        val sdfAno = java.text.SimpleDateFormat("yyyy")
        val mesActual = sdfMes.format(java.util.Date())
        val anoActual = sdfAno.format(java.util.Date())

        val cardLibrosAno = findViewById<View>(R.id.cardLibrosAno).findViewById<TextView>(R.id.tvNumero)
        val cardLibrosMes = findViewById<View>(R.id.cardLibrosMes).findViewById<TextView>(R.id.tvNumero)
        val cardPaginasAno = findViewById<View>(R.id.cardPaginasAno).findViewById<TextView>(R.id.tvNumero)
        val cardPaginasMes = findViewById<View>(R.id.cardPaginasMes).findViewById<TextView>(R.id.tvNumero)

        val graphLibrosMes = findViewById<View>(R.id.graficaLibrosMes).findViewById<View>(R.id.graphBarra)
        val graphLibrosAno = findViewById<View>(R.id.graficaLibrosAno).findViewById<View>(R.id.graphBarra)
        val graphPaginasMes = findViewById<View>(R.id.graficaPaginasMes).findViewById<View>(R.id.graphBarra)
        val graphPaginasAno = findViewById<View>(R.id.graficaPaginasAno).findViewById<View>(R.id.graphBarra)
        val graphCategorias = findViewById<View>(R.id.graficaCategorias).findViewById<View>(R.id.graphPastel)
        val graphGeneros = findViewById<View>(R.id.graficaGeneros).findViewById<View>(R.id.graphPastel)

        firestore.collection("usuarios")
            .document(userId)
            .collection("estadisticas")
            .document("general")
            .get()
            .addOnSuccessListener { stats ->
                val librosMesMap = (stats.get("librosPorMes") as? Map<String, Long>).orEmpty()
                val librosAnoMap = (stats.get("librosPorAno") as? Map<String, Long>).orEmpty()
                val paginasMesMap = (stats.get("paginasPorMes") as? Map<String, Long>).orEmpty()
                val paginasAnoMap = (stats.get("paginasPorAno") as? Map<String, Long>).orEmpty()

                cardLibrosMes.text = (librosMesMap[mesActual] ?: 0).toString()
                cardLibrosAno.text = (librosAnoMap[anoActual] ?: 0).toString()

                cardPaginasMes.text = (paginasMesMap[mesActual] ?: 0).toString()
                cardPaginasAno.text = (paginasAnoMap[anoActual] ?: 0).toString()

                graphLibrosMes.background = FullBarChartDrawable(this, crearListaParaBar(librosMesMap))
                graphLibrosAno.background = FullBarChartDrawable(this, crearListaParaBar(librosAnoMap))

                graphPaginasMes.background = FullBarChartDrawable(this, crearListaParaBar(paginasMesMap))
                graphPaginasAno.background = FullBarChartDrawable(this, crearListaParaBar(paginasAnoMap))

                cargarCategoriasYGeneros(graphCategorias, graphGeneros)
            }
    }

    private fun cargarCategoriasYGeneros(graphCategorias: View, graphGeneros: View) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(userId)
            .collection("libros")
            .whereEqualTo("estadoLectura", "Terminado")
            .get()
            .addOnSuccessListener { snap ->

                val categorias = mutableMapOf<String, Long>()
                val generos = mutableMapOf<String, Long>()

                for (doc in snap.documents) {
                    val cat = doc.getString("categoria") ?: "Sin categoría"
                    val gen = doc.getString("genero") ?: "Sin género"

                    categorias[cat] = (categorias[cat] ?: 0L) + 1L
                    generos[gen] = (generos[gen] ?: 0L) + 1L
                }

                graphCategorias.background = FullPieChartDrawable(
                    this, crearListaParaPie(categorias)
                )

                graphGeneros.background = FullPieChartDrawable(
                    this, crearListaParaPie(generos)
                )
            }
    }


    private fun convertirMapaEnCategoriasInt(mapa: Map<String, Int>): ArrayList<Category> {
        val lista = ArrayList<Category>()
        mapa.forEach { (key, value) ->
            lista.add(
                Category(key, value.toFloat(), R.color.greenie, value.toFloat())
            )
        }
        return lista
    }

    // Convierte Map<String, Long> -> ArrayList<Category> para gráficas de PASTEL
    private fun crearListaParaPie(mapa: Map<String, Long>, colorFallback: Int = R.color.blue): ArrayList<Category> {
        val lista = ArrayList<Category>()
        val total = mapa.values.sum().toFloat()
        if (total <= 0f) return lista

        // colores rotativos si quieres (puedes mejorar la paleta)
        val colores = listOf(R.color.blue, R.color.orange, R.color.greenie, R.color.purple_200, R.color.teal_200)

        var idx = 0
        mapa.forEach { (key, value) ->
            val porcentaje = (value.toFloat() / total) * 100f
            val color = colores.getOrNull(idx % colores.size) ?: colorFallback
            lista.add(Category(key, porcentaje, color, value.toFloat()))
            idx++
        }
        return lista
    }

    // Convierte Map<String, Long> -> ArrayList<Category> para gráficas de BARRA
// Aquí 'porcentaje' será relativo al valor máximo (0..100)
    private fun crearListaParaBar(mapa: Map<String, Long>, colorFallback: Int = R.color.blue): ArrayList<Category> {
        val lista = ArrayList<Category>()
        if (mapa.isEmpty()) return lista

        val maxVal = mapa.values.maxOrNull()?.toFloat() ?: 0f
        if (maxVal <= 0f) {
            // todos 0 -> meter items con 0%
            var idx0 = 0
            val colores = listOf(R.color.blue, R.color.orange, R.color.greenie)
            mapa.forEach { (k, v) ->
                val color = colores.getOrNull(idx0 % colores.size) ?: colorFallback
                lista.add(Category(k, 0f, color, v.toFloat()))
                idx0++
            }
            return lista
        }

        val colores = listOf(R.color.blue, R.color.orange, R.color.greenie, R.color.purple_200, R.color.teal_200)
        var idx = 0
        mapa.forEach { (key, value) ->
            val porcentaje = (value.toFloat() / maxVal) * 100f
            val color = colores.getOrNull(idx % colores.size) ?: colorFallback
            lista.add(Category(key, porcentaje, color, value.toFloat()))
            idx++
        }
        return lista
    }


}

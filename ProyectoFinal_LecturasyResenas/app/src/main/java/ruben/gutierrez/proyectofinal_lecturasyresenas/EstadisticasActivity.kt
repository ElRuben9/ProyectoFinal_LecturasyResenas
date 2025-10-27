package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import ruben.gutierrez.proyectofinal_lecturasyresenas.utilities.CustomBarDrawable
import ruben.gutierrez.proyectofinal_lecturasyresenas.utilities.CustomCircleDrawable
import ruben.gutierrez.proyectofinal_lecturasyresenas.utilities.Emociones

class EstadisticasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_estadisticas)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)


        findViewById<View>(R.id.cardLibrosAno).findViewById<TextView>(R.id.tvDescripcion).text = "Libros este año"
        findViewById<View>(R.id.cardLibrosMes).findViewById<TextView>(R.id.tvDescripcion).text = "Libros este mes"
        findViewById<View>(R.id.cardPaginasAno).findViewById<TextView>(R.id.tvDescripcion).text = "Páginas este año"
        findViewById<View>(R.id.cardPaginasMes).findViewById<TextView>(R.id.tvDescripcion).text = "Páginas este mes"

        findViewById<View>(R.id.cardLibrosAno).findViewById<TextView>(R.id.tvNumero).text = "24"
        findViewById<View>(R.id.cardLibrosMes).findViewById<TextView>(R.id.tvNumero).text = "3"
        findViewById<View>(R.id.cardPaginasAno).findViewById<TextView>(R.id.tvNumero).text = "7240"
        findViewById<View>(R.id.cardPaginasMes).findViewById<TextView>(R.id.tvNumero).text = "580"

        findViewById<View>(R.id.graficaLibrosMes).findViewById<TextView>(R.id.tvTituloGrafica).text = "Libros leídos por mes"
        findViewById<View>(R.id.graficaLibrosAno).findViewById<TextView>(R.id.tvTituloGrafica).text = "Libros leídos por año"
        findViewById<View>(R.id.graficaCategorias).findViewById<TextView>(R.id.tvTituloGrafica).text = "Libros por categoría"
        findViewById<View>(R.id.graficaGeneros).findViewById<TextView>(R.id.tvTituloGrafica).text = "Libros por género"
        findViewById<View>(R.id.graficaPaginasMes).findViewById<TextView>(R.id.tvTituloGrafica).text = "Páginas leídas por mes"
        findViewById<View>(R.id.graficaPaginasAno).findViewById<TextView>(R.id.tvTituloGrafica).text = "Páginas leídas por año"

        val graphLibrosMes = findViewById<View>(R.id.graficaLibrosMes).findViewById<View>(R.id.graphBarra)
        val graphLibrosAno = findViewById<View>(R.id.graficaLibrosAno).findViewById<View>(R.id.graphBarra)
        val graphCategorias = findViewById<View>(R.id.graficaCategorias).findViewById<View>(R.id.graphPastel)
        val graphGeneros = findViewById<View>(R.id.graficaGeneros).findViewById<View>(R.id.graphPastel)
        val graphPaginasMes = findViewById<View>(R.id.graficaPaginasMes).findViewById<View>(R.id.graphBarra)
        val graphPaginasAno = findViewById<View>(R.id.graficaPaginasAno).findViewById<View>(R.id.graphBarra)

        // Ejemplo de datos
        val librosPorMes = listOf(5f, 2f, 3f, 4f, 6f, 1f, 7f, 3f, 2f, 5f, 4f, 8f)
        val categorias = ArrayList(listOf(
            Emociones("Ficción", 40f, R.color.blue, 10f),
            Emociones("No Ficción", 30f, R.color.orange, 8f),
            Emociones("Fantasía", 30f, R.color.greenie, 7f)
        ))

        graphLibrosMes.background = CustomBarDrawable(this, categorias[1])
        graphLibrosAno.background = CustomBarDrawable(this, categorias[1])
        graphCategorias.background = CustomCircleDrawable(this, categorias)
        graphGeneros.background = CustomCircleDrawable(this, categorias)
        graphPaginasMes.background = CustomBarDrawable(this, categorias[1])
        graphPaginasAno.background = CustomBarDrawable(this, categorias[1])




        //Esto es para que el icono de estadísticas esté seleccionado
        bottomNavigation.selectedItemId = R.id.nav_estadisticas

        //Esto es para cambiar entre las secciones
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

    }

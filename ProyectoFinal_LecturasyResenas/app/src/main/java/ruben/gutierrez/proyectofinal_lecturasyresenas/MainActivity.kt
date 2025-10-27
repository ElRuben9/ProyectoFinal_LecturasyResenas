package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val fabAgregarLibro = findViewById<FloatingActionButton>(R.id.fab_agregar_libro)
        val botonPerfil = findViewById<ImageView>(R.id.boton_perfil)

        // Para que quede seleccionado "Biblioteca" al iniciar
        bottomNavigation.selectedItemId = R.id.nav_biblioteca

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_biblioteca -> true // Que esta clase representa la seccion de "Biblioteca"

                R.id.nav_estadisticas -> {
                    startActivity(Intent(this, EstadisticasActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }


        // Boton flotante para agregar libro
        fabAgregarLibro.setOnClickListener {
            val intent = Intent(this, AgregarLibroActivity::class.java)
            startActivity(intent)
        }

        //  Boton de perfil
        botonPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }



      }

    }

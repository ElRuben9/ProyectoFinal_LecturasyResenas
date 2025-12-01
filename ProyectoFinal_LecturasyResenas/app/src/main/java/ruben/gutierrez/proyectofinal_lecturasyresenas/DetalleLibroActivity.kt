package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import ruben.gutierrez.proyectofinal_lecturasyresenas.model.Libro
import ruben.gutierrez.proyectofinal_lecturasyresenas.utilities.StatisticsManager


class DetalleLibroActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()

    private fun registrarPaginasLeidas(paginasAntes: Int, paginasDespues: Int) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val paginasLeidas = paginasDespues - paginasAntes

        if (paginasLeidas <= 0) return

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd")
        val hoy = sdf.format(java.util.Date())

        val docRef = firestore.collection("usuarios")
            .document(userId)
            .collection("historialLectura")
            .document(hoy)

        val datos = mapOf(
            "paginasLeidas" to com.google.firebase.firestore.FieldValue.increment(paginasLeidas.toLong()),
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        docRef.set(datos, com.google.firebase.firestore.SetOptions.merge())
    }


    private lateinit var layoutProgreso: LinearLayout
    private lateinit var layoutResumen: LinearLayout
    private lateinit var layoutFinalizar: LinearLayout

    private lateinit var imgPortada: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvAutor: TextView
    private lateinit var tvCategoria: TextView
    private lateinit var tvGenero: TextView
    private lateinit var tvSinopsis: TextView
    private lateinit var tvIsbn: TextView
    private lateinit var tvResumen: TextView
    private lateinit var tvTema: TextView

    private lateinit var ratingBar: RatingBar
    private lateinit var tvValorRating: TextView

    private lateinit var etPaginas: EditText
    private lateinit var btnActualizarProgreso: Button
    private lateinit var btnIniciarLibro: Button
    private lateinit var btnMarcarCompletado: Button
    private lateinit var ratingFinal: RatingBar
    private lateinit var edtResumenFinal: EditText
    private lateinit var btnGuardarFinalizacion: Button

    private lateinit var btnEditarDatos: Button
    private lateinit var progressBar: ProgressBar

    private var libroActual: Libro? = null
    private var idLibro: String? = null

    private lateinit var libro: Libro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_libro)

        imgPortada = findViewById(R.id.imgPortada)
        tvTitulo = findViewById(R.id.tvTituloLibro)
        tvAutor = findViewById(R.id.tvAutorLibro)
        tvCategoria = findViewById(R.id.tvCategoria)
        tvGenero = findViewById(R.id.tvGenero)
        tvTema = findViewById(R.id.tvTema)
        tvSinopsis = findViewById(R.id.tvSinopsis)
        tvIsbn = findViewById(R.id.tvIsbn)
        tvResumen = findViewById(R.id.tvResumen)

        ratingBar = findViewById(R.id.ratingBar)
        tvValorRating = findViewById(R.id.tvValorRating)

        progressBar = findViewById(R.id.progresoLibro)

        layoutProgreso = findViewById(R.id.layoutProgreso)
        layoutResumen = findViewById(R.id.layoutResumen)
        layoutFinalizar = findViewById(R.id.layoutFinalizar)

        etPaginas = findViewById(R.id.etPaginasLeidas)
        btnActualizarProgreso = findViewById(R.id.btnActualizarProgreso)
        btnIniciarLibro = findViewById(R.id.btnIniciarLibro)
        btnMarcarCompletado = findViewById(R.id.btnMarcarCompletado)

        ratingFinal = findViewById(R.id.ratingBarEditar)
        edtResumenFinal = findViewById(R.id.edtResumenFinal)
        btnGuardarFinalizacion = findViewById(R.id.btnGuardarFinalizacion)

        btnEditarDatos = findViewById(R.id.btnEditarDatos)

        idLibro = intent.getStringExtra("idLibro")
        if (idLibro != null) cargarLibroDesdeFirestore(idLibro!!)

        btnIniciarLibro.setOnClickListener {
            actualizarEstadoLibro("En curso", 1)
        }

        btnActualizarProgreso.setOnClickListener {
            val pagina = etPaginas.text.toString().toIntOrNull()
            if (pagina != null) {
                actualizarPaginaActual(pagina)
            }
        }

        btnMarcarCompletado.setOnClickListener {
            mostrarFinalizacion()
        }

        btnGuardarFinalizacion.setOnClickListener {
            guardarFinalizacion()
        }

        btnEditarDatos.setOnClickListener {
            // ✔ CAMBIO: mandar solo idLibro y recargar después
            val intent = Intent(this, EditarLibroActivity::class.java)
            intent.putExtra("libroId", idLibro) // ← agregado
            startActivityForResult(intent, 300)  // ← agregado
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_detalle_libro)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    // carga los datos del libro

    private fun cargarLibroDesdeFirestore(idLibro: String) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(userId)
            .collection("libros")
            .document(idLibro)
            .get()
            .addOnSuccessListener { doc ->
                val libro = doc.toObject(Libro::class.java)
                if (libro != null) {
                    libroActual = libro
                    mostrarDatosDelLibro(libro)
                    actualizarVistaSegunEstado(libro)
                }
            }
    }

    private fun mostrarDatosDelLibro(libro: Libro) {
        tvTitulo.text = libro.titulo
        tvAutor.text = libro.autor
        tvCategoria.text = libro.categoria
        tvGenero.text = "Género: ${libro.genero ?: "N/A"}"
        tvTema.text = "Tema: ${libro.tema ?: "N/A"}"
        tvSinopsis.text = libro.sinopsis ?: "Sin sinopsis"
        tvResumen.text = libro.resumen ?: "Sin resumen"
        tvIsbn.text = "ISBN: ${libro.isbn ?: "N/A"}"

        if (!libro.portadaUri.isNullOrEmpty()) {
            Picasso.get().load(libro.portadaUri).into(imgPortada)
        }

        ratingBar.rating = libro.rating ?: 0f
        tvValorRating.text = (libro.rating ?: 0f).toString()

        progressBar.max = libro.paginas ?: 1
        progressBar.progress = libro.paginaActual ?: 0

        etPaginas.hint = "Página actual: ${libro.paginaActual ?: 0}"

    }

    private fun actualizarVistaSegunEstado(libro: Libro) {
        when (libro.estadoLectura) {
            "Por leer" -> {
                layoutProgreso.visibility = View.GONE
                btnIniciarLibro.visibility = View.VISIBLE
                btnMarcarCompletado.visibility = View.GONE
                layoutFinalizar.visibility = View.GONE
                layoutResumen.visibility = View.GONE
            }

            "En curso" -> {
                btnIniciarLibro.visibility = View.GONE
                layoutProgreso.visibility = View.VISIBLE
                btnMarcarCompletado.visibility = View.VISIBLE
                layoutFinalizar.visibility = View.GONE
                layoutResumen.visibility = View.GONE
            }

            "Terminado" -> {
                layoutProgreso.visibility = View.GONE
                btnIniciarLibro.visibility = View.GONE
                btnMarcarCompletado.visibility = View.GONE
                layoutFinalizar.visibility = View.VISIBLE
                layoutResumen.visibility = View.VISIBLE

                ratingFinal.rating = libro.rating ?: 0f
                edtResumenFinal.setText(libro.resumen ?: "")
            }
        }
    }

    private fun actualizarPaginaActual(pagina: Int) {
        val libro = libroActual ?: return
        val paginasTotales = libro.paginas ?: 1
        val paginasAntes = libro.paginaActual ?: 0

        // 1) VALIDACIÓN: NO PERMITIR NÚMERO MENOR
        if (pagina < paginasAntes) {
            Toast.makeText(this, "No puedes ingresar un número menor al progreso actual", Toast.LENGTH_SHORT).show()
            return
        }

        // 2) SI ES MAYOR O IGUAL, SE GUARDA Y SE CALCULA PÁGINAS LEÍDAS

        val paginaAnterior = libro.paginaActual ?: 0
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        val nuevoEstado =
            if (pagina >= paginasTotales) "Terminado"
            else "En curso"

        val updates = mapOf(
            "paginaActual" to pagina,
            "estadoLectura" to nuevoEstado
        )

        firestore.collection("usuarios")
            .document(userId)
            .collection("libros")
            .document(idLibro!!)
            .update(updates)
            .addOnSuccessListener {

                progressBar.progress = pagina

                // Llamadas al manager de estadisticas
                StatisticsManager().registrarPaginas(pagina - paginaAnterior)
                if(nuevoEstado == "Terminado")
                    StatisticsManager().registrarLibroLeido()

                Toast.makeText(this, "Progreso actualizado", Toast.LENGTH_SHORT).show()


                // ✔ Registrar el número de páginas leídas hoy
                registrarPaginasLeidas(paginasAntes, pagina)

                progressBar.progress = pagina
                Toast.makeText(this, "Progreso actualizado", Toast.LENGTH_SHORT).show()


                progressBar.progress = pagina

                // Llamadas al manager de estadisticas
                StatisticsManager().registrarPaginas(pagina - paginaAnterior)
                if(nuevoEstado == "Terminado")
                    StatisticsManager().registrarLibroLeido()

                Toast.makeText(this, "Progreso actualizado", Toast.LENGTH_SHORT).show()

                cargarLibroDesdeFirestore(idLibro!!)
            }
    }

    private fun actualizarEstadoLibro(nuevoEstado: String, paginaInicial: Int = 0) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(userId)
            .collection("libros")
            .document(idLibro!!)
            .update(
                mapOf(
                    "estadoLectura" to nuevoEstado,
                    "paginaActual" to paginaInicial
                )
            ).addOnSuccessListener {
                cargarLibroDesdeFirestore(idLibro!!)
            }
    }

    private fun mostrarFinalizacion() {
        layoutFinalizar.visibility = View.VISIBLE
    }

    private fun guardarFinalizacion() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val calificacion = ratingFinal.rating
        val resumenTxt = edtResumenFinal.text.toString()

        val paginasTotales = libroActual?.paginas ?: 0

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(userId)
            .collection("libros")
            .document(idLibro!!)
            .update(
                mapOf(
                    "estadoLectura" to "Terminado",
                    "rating" to calificacion,
                    "resumen" to resumenTxt,
                    "paginaActual" to paginasTotales
                )
            ).addOnSuccessListener {
                Toast.makeText(this, "Libro finalizado", Toast.LENGTH_SHORT).show()
                cargarLibroDesdeFirestore(idLibro!!)
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 300 && resultCode == RESULT_OK) {
            cargarLibroDesdeFirestore(idLibro!!)
        }
    }
}


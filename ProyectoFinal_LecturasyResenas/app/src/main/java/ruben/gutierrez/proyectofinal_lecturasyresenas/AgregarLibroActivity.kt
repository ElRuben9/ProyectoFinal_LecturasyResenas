package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ruben.gutierrez.proyectofinal_lecturasyresenas.model.Libro
import ruben.gutierrez.proyectofinal_lecturasyresenas.utilities.StatisticsManager
import java.io.File

class AgregarLibroActivity : AppCompatActivity() {


    private var portadaUri: Uri? = null

    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {

                val uri = result.data!!.data
                if (uri != null) {

                    val takeFlags = result.data!!.flags and
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    try {
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    portadaUri = uri

                    findViewById<ImageView>(R.id.imgPortadaPreview).setImageURI(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        val isbn = findViewById<EditText>(R.id.edtISBN)
        val titulo = findViewById<EditText>(R.id.edtTitulo)
        val autor = findViewById<EditText>(R.id.edtAutor)
        val categoria = findViewById<Spinner>(R.id.spnCategoria)
        val tema = findViewById<Spinner>(R.id.spnTema)
        val genero = findViewById<Spinner>(R.id.spnGenero)
        val paginas = findViewById<EditText>(R.id.edtNumeroPaginas)
        val sinopsis = findViewById<EditText>(R.id.edtSinopsis)
        val paginaActual = findViewById<EditText>(R.id.edtPaginaActual)
        val btnSeleccionarImg = findViewById<Button>(R.id.btnSeleccionarImagen)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarLibro)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        //ESTO ES PARA LO DEL ESTADO DE LECTURAAA
        val estadoLectura = findViewById<Spinner>(R.id.spnEstadoLectura)
        val resumen = findViewById<EditText>(R.id.edtResumen)
        val rating = findViewById<RatingBar>(R.id.ratingBar)

        estadoLectura.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (estadoLectura.selectedItem.toString()) {

                    "Por leer" -> {
                        paginaActual.visibility = View.GONE
                        resumen.visibility = View.GONE
                        rating.visibility = View.GONE
                    }

                    "En curso" -> {
                        paginaActual.visibility = View.VISIBLE
                        resumen.visibility = View.GONE
                        rating.visibility = View.GONE
                    }

                    "Terminado" -> {
                        paginaActual.visibility = View.GONE
                        resumen.visibility = View.VISIBLE
                        rating.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        btnSeleccionarImg.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            seleccionarImagen.launch(intent)
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_add_book)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnGuardar.setOnClickListener {

            if (titulo.text.isBlank() || autor.text.isBlank()) {
                Toast.makeText(this, "Título y autor son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoriaSeleccionada = categoria.selectedItem.toString()

// Esto es para normalizar
// Si la categoria selecionada es academico, genero = null
            val temaFinal = if (categoriaSeleccionada == "Académico") {
                tema.selectedItem?.toString()
            } else {
                null
            }
// Si la categoria seleccionada es Ficcion o No Ficcion, tema = null.
            val generoFinal = if (categoriaSeleccionada == "Ficción" || categoriaSeleccionada == "No Ficción") {
                genero.selectedItem?.toString()
            } else {
                null
            }

            //obtiene el estado del spinner
            val estado = estadoLectura.selectedItem.toString()

            val paginasTotales = paginas.text.toString().toIntOrNull() ?: 0

// aqui calcula la pagina actual segun el estado
            val paginaActualFinal = when (estado) {

                "Por leer" -> 0

                "En curso" ->
                    paginaActual.text.toString().toIntOrNull()?.coerceAtMost(paginasTotales) ?: 0

                "Terminado" ->
                    paginasTotales  // se marca como completado
                else -> 0
            }

            val resumenFinal = if (estado == "Terminado") resumen.text.toString() else null
            val ratingFinal = if (estado == "Terminado") rating.rating else null




            val libro = Libro(
                isbn = isbn.text.toString().ifBlank { null },
                titulo = titulo.text.toString(),
                autor = autor.text.toString(),
                categoria = categoriaSeleccionada,
                tema = temaFinal,
                genero = generoFinal,
                paginas = paginas.text.toString().toIntOrNull(),
                sinopsis = sinopsis.text.toString(),

                estadoLectura = estado,
                paginaActual = paginaActualFinal,
                resumen = resumenFinal,
                rating = ratingFinal,

                portadaUri = null,
                userId = uid
            )

            if (portadaUri != null) {
                subirImagenACloudinary(libro)
            } else {
                guardarLibroEnFirestore(libro)
            }

        }

        btnCancelar.setOnClickListener { finish() }




        categoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val categoriaSeleccionada = categoria.selectedItem.toString()

                val txtTemaLabel = findViewById<TextView>(R.id.txtTemaLabel)
                val txtGeneroLabel = findViewById<TextView>(R.id.txtGeneroLabel)
                val txtCategoria = findViewById<TextView>(R.id.categoria)


                when (categoriaSeleccionada) {

                    "Selecciona una categoría" -> {
                        // no muestra nada
                        txtTemaLabel.visibility = View.GONE
                        tema.visibility = View.GONE


                        txtGeneroLabel.visibility = View.GONE
                        genero.visibility = View.GONE
                    }


                    "Académico" -> {
                        // este muestra el tema y quita el genero
                        txtTemaLabel.visibility = View.VISIBLE
                        tema.visibility = View.VISIBLE


                        txtGeneroLabel.visibility = View.GONE
                        genero.visibility = View.GONE
                    }

                    "Ficción", "No Ficción" -> {
                        // este muestra el genero y quita el tema
                        txtGeneroLabel.visibility = View.VISIBLE
                        genero.visibility = View.VISIBLE


                        txtTemaLabel.visibility = View.GONE
                        tema.visibility = View.GONE
                    }


                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


    }


    private fun subirImagenACloudinary(libro: Libro) {
        val uri = portadaUri ?: return

        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show()


        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("portada", ".jpg", cacheDir)
        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }

        MediaManager.get().upload(tempFile.absolutePath)
            .unsigned("libros_preset")
            .option("folder", "libros/")
            .callback(object : UploadCallback {

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String

                    if (url != null) {
                        val libroConImagen = libro.copy(portadaUri = url)
                        guardarLibroEnFirestore(libroConImagen)
                    } else {
                        Toast.makeText(
                            this@AgregarLibroActivity,
                            "No se obtuvo URL de portada",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(
                        this@AgregarLibroActivity,
                        "Error subiendo imagen: ${error?.description}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    private fun guardarLibroEnFirestore(libro: Libro) {
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = firestore.collection("usuarios")
            .document(userId)
            .collection("libros")
            .document()

        val libroConId = libro.copy(id = ref.id)

        ref.set(libroConId)
            .addOnSuccessListener {

                StatisticsManager().registrarPaginas(libro.paginaActual!!)
                if(libro.estadoLectura == "Terminado")
                    StatisticsManager().registrarLibroLeido()


                Toast.makeText(this, "Libro guardado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error guardando el libro", Toast.LENGTH_SHORT).show()
            }

    }
}

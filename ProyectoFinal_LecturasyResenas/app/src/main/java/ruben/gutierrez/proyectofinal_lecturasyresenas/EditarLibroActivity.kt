package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import ruben.gutierrez.proyectofinal_lecturasyresenas.model.Libro

class EditarLibroActivity : AppCompatActivity() {

    private val PICK_IMAGE = 200

    private lateinit var imgPortada: ImageView
    private lateinit var etTitulo: EditText
    private lateinit var etAutor: EditText
    private lateinit var etIsbn: EditText
    private lateinit var etSinopsis: EditText
    private lateinit var btnGuardar: Button

    private var nuevaPortadaUri: Uri? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    private lateinit var libroId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_libro)

        // recibimos solo el ID
        libroId = intent.getStringExtra("libroId") ?: run { finish(); return }

        imgPortada = findViewById(R.id.imgPortadaEditar)
        etTitulo = findViewById(R.id.etTituloEditar)
        etAutor = findViewById(R.id.etAutorEditar)
        etIsbn = findViewById(R.id.etIsbnEditar)
        etSinopsis = findViewById(R.id.etSinopsisEditar)
        btnGuardar = findViewById(R.id.btnGuardarCambios)

        cargarDatos()

        imgPortada.setOnClickListener { seleccionarImagen() }
        btnGuardar.setOnClickListener { guardarCambios() }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_editar_libro)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    // Obtiene datos reales desde Firestore
    private fun cargarDatos() {
        firestore.collection("usuarios")
            .document(userId)
            .collection("libros")
            .document(libroId)
            .get()
            .addOnSuccessListener { doc ->
                val libro = doc.toObject(Libro::class.java) ?: return@addOnSuccessListener

                if (!libro.portadaUri.isNullOrEmpty()) {
                    Picasso.get().load(libro.portadaUri).into(imgPortada)
                }
                etTitulo.setText(libro.titulo)
                etAutor.setText(libro.autor)
                etIsbn.setText(libro.isbn ?: "")
                etSinopsis.setText(libro.sinopsis ?: "")
            }
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            nuevaPortadaUri = data?.data

            contentResolver.takePersistableUriPermission(
                nuevaPortadaUri!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            imgPortada.setImageURI(nuevaPortadaUri)
        }
    }

    private fun guardarCambios() {
        val titulo = etTitulo.text.toString().trim()
        val autor = etAutor.text.toString().trim()
        val isbn = etIsbn.text.toString().trim()
        val sinopsis = etSinopsis.text.toString().trim()

        if (titulo.isEmpty() || autor.isEmpty()) {
            Toast.makeText(this, "Revisa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (nuevaPortadaUri == null) {
            actualizarFirestore(null, titulo, autor, isbn, sinopsis)
        } else {
            subirImagenACloudinary(titulo, autor, isbn, sinopsis)
        }
    }

    private fun subirImagenACloudinary(
        titulo: String,
        autor: String,
        isbn: String,
        sinopsis: String,
    ) {
        val uri = nuevaPortadaUri ?: return

        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(uri)
            .unsigned("libros_preset")
            .option("folder", "libros/")
            .callback(object : UploadCallback {

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    actualizarFirestore(url, titulo, autor, isbn, sinopsis)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(
                        this@EditarLibroActivity,
                        "Error al subir la portada: ${error?.description}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}

            })
            .dispatch()
    }

    private fun actualizarFirestore(
        portadaUrl: String?,
        titulo: String,
        autor: String,
        isbn: String,
        sinopsis: String,
    ) {
        val datos = mutableMapOf<String, Any>()

        datos["titulo"] = titulo
        datos["autor"] = autor
        datos["isbn"] = isbn
        datos["sinopsis"] = sinopsis

        if (portadaUrl != null) datos["portadaUri"] = portadaUrl

        firestore.collection("usuarios")
            .document(userId)
            .collection("libros")
            .document(libroId)
            .update(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()

                setResult(RESULT_OK) // notifica a DetalleLibroActivity para que no se pongan los datos mock
                finish()
            }
    }
}


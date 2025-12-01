package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var nuevaImagenUri: Uri? = null

    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                nuevaImagenUri = result.data!!.data
                findViewById<ImageView>(R.id.imgPerfil).setImageURI(nuevaImagenUri)
                subirImagenACloudinary()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_add_book)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Referencias UI
        val tvNombre = findViewById<TextView>(R.id.tvNombre)
        val tvCorreo = findViewById<TextView>(R.id.tvCorreo)
        val tvFecha = findViewById<TextView>(R.id.tvFechaNacimiento)
        val tvProfesion = findViewById<TextView>(R.id.tvProfesion)
        val tvGenero = findViewById<TextView>(R.id.tvGenero)

        val edtNombre = findViewById<EditText>(R.id.edtNombreEdit)
        val edtFecha = findViewById<EditText>(R.id.edtFechaEdit)
        val edtProfesion = findViewById<EditText>(R.id.edtProfesionEdit)
        val spinnerGenero = findViewById<Spinner>(R.id.spinnerGenero)

        val btnEditar = findViewById<Button>(R.id.btnEditar)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarCambios)
        val btnCambiarContrasena = findViewById<Button>(R.id.btnCambiarContrasena)

        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)
        val tvCambiarFoto = findViewById<TextView>(R.id.tvCambiarFoto)

        val generos = resources.getStringArray(R.array.generos_usuario)
        val adaptadorGenero = ArrayAdapter(this, android.R.layout.simple_spinner_item, generos)
        adaptadorGenero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGenero.adapter = adaptadorGenero

        val tvLibrosLeidos = findViewById<TextView>(R.id.tvLibrosLeidos)
        val tvPaginasHoy = findViewById<TextView>(R.id.tvPaginasHoy)
        val tvDiasSeguidos = findViewById<TextView>(R.id.tvDiasSeguidos)



        val userId = auth.currentUser?.uid ?: return
        val userRef = database.reference.child("Usuarios").child(userId)


        findViewById<Button>(R.id.btnCambiarContrasena).setOnClickListener {
            startActivity(Intent(this, CambiarPassword::class.java))
        }

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombre").value.toString()
                    val correo = snapshot.child("correo").value.toString()
                    val fecha = snapshot.child("fechaNacimiento").value.toString()
                    val profesion = snapshot.child("profesion").value.toString()
                    val genero = snapshot.child("genero").value.toString()
                    val fotoUrl = snapshot.child("fotoPerfil").value.toString()

                    // Modo lectura
                    tvNombre.text = nombre
                    tvCorreo.text = correo
                    tvFecha.text = fecha
                    tvProfesion.text = profesion
                    tvGenero.text = genero

                    // Modo edición
                    edtNombre.setText(nombre)
                    edtFecha.setText(fecha)
                    edtProfesion.setText(profesion)

                    val generos = resources.getStringArray(R.array.generos_usuario)
                    spinnerGenero.setSelection(generos.indexOf(genero))

                    if (fotoUrl.isNotEmpty()) {
                        Glide.with(this@PerfilActivity)
                            .load(fotoUrl)
                            .into(imgPerfil)
                    }

                } else {
                    Toast.makeText(this@PerfilActivity, "No se encontraron datos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PerfilActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })


        tvCambiarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            seleccionarImagen.launch(intent)
        }

        edtFecha.setOnClickListener {
            val cal = Calendar.getInstance()
            val dp = DatePickerDialog(
                this,
                { _, y, m, d ->
                    edtFecha.setText("%02d/%02d/%04d".format(d, m + 1, y))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dp.datePicker.maxDate = System.currentTimeMillis()
            dp.show()
        }


        btnEditar.setOnClickListener {
            cambiarModoEdicion(true)
        }


        btnGuardar.setOnClickListener {

            val nuevoNombre = edtNombre.text.toString().trim()
            val nuevaFecha = edtFecha.text.toString().trim()
            val nuevaProfesion = edtProfesion.text.toString().trim()
            val nuevoGenero = spinnerGenero.selectedItem.toString()

            if (nuevoNombre.isEmpty() || nuevaFecha.isEmpty() || nuevaProfesion.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = mapOf(
                "nombre" to nuevoNombre,
                "fechaNacimiento" to nuevaFecha,
                "profesion" to nuevaProfesion,
                "genero" to nuevoGenero
            )

            userRef.updateChildren(updates)
                .addOnSuccessListener {

                    // Actualizar modo lectura
                    tvNombre.text = nuevoNombre
                    tvFecha.text = nuevaFecha
                    tvProfesion.text = nuevaProfesion
                    tvGenero.text = nuevoGenero

                    cambiarModoEdicion(false)

                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }
        cargarEstadisticas()
    }

    private fun cargarEstadisticas() {
        val userId = auth.currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        val tvLibrosLeidos = findViewById<TextView>(R.id.tvLibrosLeidos)
        val tvPaginasHoy = findViewById<TextView>(R.id.tvPaginasHoy)
        val tvDiasSeguidos = findViewById<TextView>(R.id.tvDiasSeguidos)

        // Libros leídos
        firestore.collection("usuarios")
            .document(userId)
            .collection("libros")
            .whereEqualTo("estadoLectura", "Terminado")
            .get()
            .addOnSuccessListener { snap ->
                tvLibrosLeidos.text = snap.size().toString()
            }

        // Estadísticas generales
        firestore.collection("usuarios")
            .document(userId)
            .collection("estadisticas")
            .document("general")
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {

                    // PÁGINAS HOY
                    val paginasHoy = (doc.getLong("paginasHoy") ?: 0).toInt()
                    tvPaginasHoy.text = paginasHoy.toString()

                    // RACHA
                    val racha = (doc.getLong("rachaDias") ?: 1).toInt()
                    tvDiasSeguidos.text = racha.toString()

                } else {
                    // Si el doc no existe aún → mostrar 0 en todo
                    tvPaginasHoy.text = "0"
                    tvDiasSeguidos.text = "0"
                }
            }
    }

    private fun subirImagenACloudinary() {
        val uri = nuevaImagenUri ?: return

        Toast.makeText(this, "Subiendo foto...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(uri)
            .unsigned("perfiles_preset")
            .option("folder", "perfiles/")
            .callback(object : UploadCallback {

                override fun onStart(requestId: String?) {}

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val secureUrl = resultData?.get("secure_url")?.toString() ?: return
                    guardarUrlEnFirebase(secureUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(
                        this@PerfilActivity,
                        "Error al subir: ${error?.description}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    private fun guardarUrlEnFirebase(url: String) {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("Usuarios").child(userId).child("fotoPerfil")
            .setValue(url)
            .addOnSuccessListener {
                Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error guardando URL", Toast.LENGTH_SHORT).show()
            }
    }


    private fun cambiarModoEdicion(editar: Boolean) {

        val tvNombre = findViewById<TextView>(R.id.tvNombre)
        val tvFecha = findViewById<TextView>(R.id.tvFechaNacimiento)
        val tvProfesion = findViewById<TextView>(R.id.tvProfesion)
        val tvGenero = findViewById<TextView>(R.id.tvGenero)

        val edtNombre = findViewById<EditText>(R.id.edtNombreEdit)
        val edtFecha = findViewById<EditText>(R.id.edtFechaEdit)
        val edtProfesion = findViewById<EditText>(R.id.edtProfesionEdit)
        val spinnerGenero = findViewById<Spinner>(R.id.spinnerGenero)

        val btnEditar = findViewById<Button>(R.id.btnEditar)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarCambios)

        if (editar) {
            tvNombre.visibility = TextView.GONE
            tvFecha.visibility = TextView.GONE
            tvProfesion.visibility = TextView.GONE
            tvGenero.visibility = TextView.GONE

            edtNombre.visibility = EditText.VISIBLE
            edtFecha.visibility = EditText.VISIBLE
            edtProfesion.visibility = EditText.VISIBLE
            spinnerGenero.visibility = Spinner.VISIBLE

            btnEditar.visibility = Button.GONE
            btnGuardar.visibility = Button.VISIBLE

        } else {
            tvNombre.visibility = TextView.VISIBLE
            tvFecha.visibility = TextView.VISIBLE
            tvProfesion.visibility = TextView.VISIBLE
            tvGenero.visibility = TextView.VISIBLE

            edtNombre.visibility = EditText.GONE
            edtFecha.visibility = EditText.GONE
            edtProfesion.visibility = EditText.GONE
            spinnerGenero.visibility = Spinner.GONE

            btnEditar.visibility = Button.VISIBLE
            btnGuardar.visibility = Button.GONE
        }
    }
}

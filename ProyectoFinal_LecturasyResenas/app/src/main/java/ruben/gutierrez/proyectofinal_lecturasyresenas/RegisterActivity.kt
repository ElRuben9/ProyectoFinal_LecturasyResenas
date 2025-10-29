package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        val nombre = findViewById<EditText>(R.id.edtNombre)
        val fechaNacimiento = findViewById<EditText>(R.id.edtfechaNacimiento)
        val profesion = findViewById<EditText>(R.id.edtProfesion)
        val correo = findViewById<EditText>(R.id.edtCorreoRegistro)
        val pass = findViewById<EditText>(R.id.edtContrasenaRegistro)
        val pass2 = findViewById<EditText>(R.id.edtConfirmarContrasena)
        val radioMasculino = findViewById<RadioButton>(R.id.radioMasculino)
        val radioFemenino = findViewById<RadioButton>(R.id.radioFemenino)
        val btnRegistro = findViewById<Button>(R.id.btnRegistrarme)
        val linkLogin = findViewById<TextView>(R.id.txtEnlaceLogin)

        linkLogin.setOnClickListener {
            finish() // Regresa al login
        }

        btnRegistro.setOnClickListener {
            val nombreTxt = nombre.text.toString().trim()
            val fechaTxt = fechaNacimiento.text.toString().trim()
            val profesionTxt = profesion.text.toString().trim()
            val email = correo.text.toString().trim()
            val password = pass.text.toString().trim()
            val password2 = pass2.text.toString().trim()

            // Validaciones
            if (nombreTxt.isEmpty() || fechaTxt.isEmpty() || profesionTxt.isEmpty() ||
                email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != password2) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val generoStr = when {
                radioMasculino.isChecked -> "Masculino"
                radioFemenino.isChecked -> "Femenino"
                else -> {
                    Toast.makeText(this, "Selecciona un género", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user?.uid ?: return@addOnSuccessListener

                    // Crear objeto Usuarios para guardar en la base de datos
                    val usuario = Usuarios(
                        nombre = nombreTxt,
                        correo = email,
                        fechaNacimiento = fechaTxt,
                        genero = generoStr,
                        profesion = profesionTxt,
                        fotoPerfil = "" // Aquí añadir el link de la foto
                    )

                    // AQUÍ se guarda la información en la tabla "Usuarios"
                    database.reference.child("Usuarios").child(userId).setValue(usuario)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registro completado correctamente", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("FirebaseDB", "Error al guardar: ${e.message}")
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al registrar: ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("FirebaseAuth", "Error al registrar: ${it.message}")
                }
        }
    }
}

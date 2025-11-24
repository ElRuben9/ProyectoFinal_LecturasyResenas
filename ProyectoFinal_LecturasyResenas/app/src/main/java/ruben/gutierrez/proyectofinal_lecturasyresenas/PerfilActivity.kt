package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_add_book)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val tvNombre = findViewById<TextView>(R.id.tvNombre)
        val tvCorreo = findViewById<TextView>(R.id.tvCorreo)
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)

        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = database.reference.child("Usuarios").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombre").value.toString()
                    val correo = snapshot.child("correo").value.toString()
                    val fotoUrl = snapshot.child("fotoPerfil").value.toString()

                    tvNombre.text = nombre
                    tvCorreo.text = correo
                } else {
                    Toast.makeText(this@PerfilActivity, "No se encontraron datos", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PerfilActivity, "Error al leer datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        val tvEditarNombre = findViewById<TextView>(R.id.tvEditarNombre)

        tvEditarNombre.setOnClickListener {

            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Editar nombre")

            val input = android.widget.EditText(this)
            input.hint = "Nuevo nombre"
            input.setPadding(40, 30, 40, 30)

            builder.setView(input)

            builder.setPositiveButton("Guardar") { _, _ ->

                val nuevoNombre = input.text.toString().trim()

                if (nuevoNombre.isEmpty()) {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val userRef = database.reference.child("Usuarios").child(userId)

                userRef.child("nombre").setValue(nuevoNombre)
                    .addOnSuccessListener {
                        tvNombre.text = nuevoNombre
                        Toast.makeText(this, "Nombre actualizado correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
            }
            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }

    }
}

package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val correo = findViewById<EditText>(R.id.edtCorreo)
        val pass = findViewById<EditText>(R.id.edtContrasena)
        val btnLogin: Button = findViewById(R.id.btnEntrar)
        val linkRegistro = findViewById<TextView>(R.id.txtEnlaceRegistro)

        linkRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }


        val txtErrorLogin = findViewById<TextView>(R.id.txtErrorLogin)

        btnLogin.setOnClickListener {
            val email = correo.text.toString().trim()
            val password = pass.text.toString().trim()

            txtErrorLogin.visibility = TextView.GONE

            if (email.isEmpty() || password.isEmpty()) {
                txtErrorLogin.text = "Por favor, completa todos los campos"
                txtErrorLogin.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    txtErrorLogin.visibility = TextView.GONE
                    Toast.makeText(this, "Bienvenid@", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    txtErrorLogin.text = "Correo o contraseña incorrectos"
                    txtErrorLogin.visibility = TextView.VISIBLE
                }
        }

    }
}

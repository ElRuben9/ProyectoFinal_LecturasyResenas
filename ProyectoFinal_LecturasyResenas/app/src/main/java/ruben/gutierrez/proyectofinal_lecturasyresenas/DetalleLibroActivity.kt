package ruben.gutierrez.proyectofinal_lecturasyresenas

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.String


class DetalleLibroActivity : AppCompatActivity() {
    private var layoutProgreso: LinearLayout? = null
    private var layoutResumen: LinearLayout? = null
    private lateinit var ratingBar: RatingBar
    private lateinit var tvValorRating: TextView

    private val estadoLibro = "terminado"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_libro)

        layoutProgreso = findViewById<LinearLayout?>(R.id.layoutProgreso)
        layoutResumen = findViewById<LinearLayout?>(R.id.layoutResumen)
        ratingBar = findViewById(R.id.ratingBar);
        tvValorRating = findViewById(R.id.tvValorRating);

        actualizarVistaSegunEstado()
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            tvValorRating.text = rating.toString()
        }
    }

    private fun actualizarVistaSegunEstado() {
        if (estadoLibro == "terminado") {
            layoutProgreso!!.setVisibility(View.GONE)
            layoutResumen!!.setVisibility(View.VISIBLE)
        } else {
            layoutProgreso!!.setVisibility(View.VISIBLE)
            layoutResumen!!.setVisibility(View.GONE)
        }
    }
}
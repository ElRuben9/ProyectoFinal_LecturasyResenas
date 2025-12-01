package ruben.gutierrez.proyectofinal_lecturasyresenas.utilities

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class FullBarChartDrawable(
    private val context: Context,
    private val items: List<Category>
) : Drawable() {

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 42f
        isAntiAlias = true
    }

    private val barPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val barHeight = 70f
    private val barSpacing = 20f
    private val sidePadding = 30f

    override fun draw(canvas: Canvas) {

        // Centrado vertical de todas las barras juntas
        val totalHeight = items.size * (barHeight + barSpacing)
        var y = (canvas.height - totalHeight) / 2f

        val fm = textPaint.fontMetrics
        val textOffset = (fm.bottom - fm.top) / 2 - fm.bottom

        for (item in items) {

            val valorEntero = item.peso.toInt()

            // ------------------------------------------
            // 1) CALCULAR GEOMETRÍA DE LA BARRA
            // ------------------------------------------

            val barLeft = sidePadding + 200f   // espacio reservado para la etiqueta
            val barTop = y + 5f
            val barRight = canvas.width - sidePadding - 150f
            val barBottom = barTop + barHeight

            // ------------------------------------------
            // 2) DIBUJAR TEXTO A LA IZQUIERDA
            // ------------------------------------------

            val textX = sidePadding
            val textY = barTop + barHeight / 2 + textOffset

            canvas.drawText(item.nombre, textX, textY, textPaint)

            // ------------------------------------------
            // 3) BARRA DE FONDO
            // ------------------------------------------

            canvas.drawRect(barLeft, barTop, barRight, barBottom, backgroundPaint)

            // ------------------------------------------
            // 4) BARRA DE COLOR SEGÚN PORCENTAJE
            // ------------------------------------------

            val porcentaje = item.porcentaje.coerceIn(0f, 100f)
            val filledWidth = barLeft + (porcentaje / 100f) * (barRight - barLeft)

            barPaint.color = ContextCompat.getColor(context, item.color)
            canvas.drawRect(barLeft, barTop, filledWidth, barBottom, barPaint)

            // ------------------------------------------
            // 5) PORCENTAJE A LA DERECHA
            // ------------------------------------------

            val percentText = "${valorEntero}"
            canvas.drawText(
                percentText,
                barRight + 30f,
                textY,
                textPaint
            )

            // Avanzar a la siguiente barra
            y = barBottom + barSpacing
        }
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(cf: ColorFilter?) {}
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}

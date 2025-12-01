package ruben.gutierrez.proyectofinal_lecturasyresenas.utilities

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class FullPieChartDrawable(
    private val context: Context,
    private val items: List<Category>
) : Drawable() {

    private val legendTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 38f
        isAntiAlias = true
    }

    private val legendColorPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val slicePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.LTGRAY
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        // Zona izquierda para el pastel
        val chartWidth = width * 0.40f
        val radius = (minOf(chartWidth, height) * 0.45f)

        val centerX = chartWidth / 2
        val centerY = height / 2

        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // ---------------- FONDO DEL PASTEL ----------------
        canvas.drawArc(rect, 0f, 360f, true, backgroundPaint)

        // ---------------- DIBUJAR SECCIONES ----------------
        var startAngle = -90f

        for (item in items) {
            val sweep = (item.porcentaje * 360f) / 100f

            slicePaint.color = ContextCompat.getColor(context, item.color)

            canvas.drawArc(rect, startAngle, sweep, true, slicePaint)

            startAngle += sweep
        }

        // ---------------- LEYENDA A LA DERECHA ----------------
        drawLegend(canvas, chartWidth, height)
    }

    private fun drawLegend(canvas: Canvas, chartWidth: Float, height: Float) {
        val startX = chartWidth + 40f

        val colorBoxSize = 35f
        val colorBoxMargin = 20f
        val lineSpacing = 55f

        val totalHeight = items.size * lineSpacing
        val startY = (height - totalHeight) / 2f

        var posY = startY

        for (item in items) {

            val valorEntero = item.peso.toInt()

            // Cuadrado de color
            legendColorPaint.color = ContextCompat.getColor(context, item.color)
            canvas.drawRect(
                startX,
                posY,
                startX + colorBoxSize,
                posY + colorBoxSize,
                legendColorPaint
            )

            // Texto
            val text = "${item.nombre}  (${valorEntero})"
            canvas.drawText(
                text,
                startX + colorBoxSize + colorBoxMargin,
                posY + colorBoxSize - 5f,
                legendTextPaint
            )

            posY += lineSpacing
        }
    }

    override fun setAlpha(alpha: Int) {}
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    override fun setColorFilter(colorFilter: ColorFilter?) {}
}

package com.example.core.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R

/** Draws a [StatusCard] onto a 1080x1920 bitmap with plain Canvas, so it needs no attached view. */
object StatusCardRenderer {
    const val WIDTH = 1080
    const val HEIGHT = 1920
    private const val MARGIN = 110
    // WhatsApp draws its own bars over the top and bottom of a status, so content stays inside this band.
    private const val TOP = 300
    private const val BOTTOM = HEIGHT - 300
    private const val GAP = 60
    private const val GREEN_DARK = 0xFF0B3D2E.toInt()
    private const val GREEN = 0xFF14614A.toInt()
    private const val GOLD = 0xFFD4AF37.toInt()
    private const val CREAM = 0xFFFFF8E7.toInt()

    fun render(context: Context, card: StatusCard): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawPaint(Paint().apply { shader = LinearGradient(0f, 0f, 0f, HEIGHT.toFloat(), GREEN_DARK, GREEN, Shader.TileMode.CLAMP) })
        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 4f; color = GOLD }
        canvas.drawRoundRect(RectF(50f, 200f, WIDTH - 50f, HEIGHT - 200f), 40f, 40f, frame)

        val width = WIDTH - 2 * MARGIN
        val arabicFont = runCatching { ResourcesCompat.getFont(context, R.font.indopak_nastaleeq) }.getOrNull() ?: Typeface.SERIF
        val reference = card.reference.takeIf { it.isNotEmpty() }?.let { layout(it, paint(36f, GOLD, Typeface.DEFAULT_BOLD), width, false) }
        val footer = card.footer.takeIf { it.isNotEmpty() }?.let { layout(it, paint(32f, CREAM, Typeface.DEFAULT), width, false) }
        val textBand = BOTTOM - TOP - (reference?.height ?: 0) - (footer?.height ?: 0) - 2 * GAP
        val arabic = card.arabic.takeIf { it.isNotEmpty() }?.let {
            fitted(it, arabicFont, CREAM, 76f, 34f, width, if (card.translation.isEmpty()) textBand else textBand * 55 / 100, true)
        }
        val translation = card.translation.takeIf { it.isNotEmpty() }?.let {
            fitted(it, Typeface.DEFAULT, CREAM, 48f, 26f, width, textBand - (arabic?.height ?: 0) - GAP, false)
        }

        val blocks = listOfNotNull(arabic, translation, reference)
        val total = blocks.sumOf { it.height } + GAP * (blocks.size - 1).coerceAtLeast(0)
        var y = (TOP + (BOTTOM - TOP - (footer?.height ?: 0) - total) / 2f).coerceAtLeast(TOP.toFloat())
        blocks.forEach { y = draw(canvas, it, y) + GAP }
        footer?.let { draw(canvas, it, (BOTTOM - it.height).toFloat()) }
        return bitmap
    }

    private fun paint(size: Float, color: Int, face: Typeface) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size; this.color = color; typeface = face }

    private fun layout(text: String, paint: TextPaint, width: Int, rtl: Boolean): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(if (rtl) TextDirectionHeuristics.RTL else TextDirectionHeuristics.FIRSTSTRONG_LTR)
            .setLineSpacing(0f, if (rtl) 1.5f else 1.2f)
            .build()

    private fun fitted(text: String, face: Typeface, color: Int, start: Float, min: Float, width: Int, maxHeight: Int, rtl: Boolean): StaticLayout {
        val size = StatusCardLayout.fitTextSize(start, min, maxHeight) { layout(text, paint(it, color, face), width, rtl).height }
        val full = layout(text, paint(size, color, face), width, rtl)
        if (full.height <= maxHeight) return full
        // Still too tall at the minimum size: keep the whole lines that fit and mark the cut.
        val lastLine = (full.getLineForVertical(maxHeight.coerceAtLeast(0)) - 1).coerceAtLeast(0)
        val kept = text.take(full.getLineEnd(lastLine)).trimEnd() + "…"
        return layout(kept, paint(size, color, face), width, rtl)
    }

    private fun draw(canvas: Canvas, layout: StaticLayout, y: Float): Float {
        canvas.save()
        canvas.translate(MARGIN.toFloat(), y)
        layout.draw(canvas)
        canvas.restore()
        return y + layout.height
    }
}

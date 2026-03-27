package com.easyrifa.domain.usecase.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.easyrifa.data.db.entity.RaffleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Generates a shareable PNG image showing the current raffle status:
 * prize image (if any), number grid colored by assignment state, and counters.
 */
class GenerateShareImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class RaffleShareData(
        val raffle: RaffleEntity,
        val assignedNumbers: Set<Int>,
        val totalRange: Int
    )

    fun execute(data: RaffleShareData): Result<Intent> = runCatching {
        val bitmap = generateBitmap(data)
        val uri = saveBitmapAndGetUri(bitmap, data.raffle.id)

        Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Estado de la rifa: ${data.raffle.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun generateBitmap(data: RaffleShareData): Bitmap {
        val raffle = data.raffle
        val assigned = data.assignedNumbers
        val range = raffle.maxNumber - raffle.minNumber + 1

        // Layout constants
        val width = 1080
        val cellSize = 60
        val padding = 40
        val cols = (width - padding * 2) / cellSize
        val rows = (range + cols - 1) / cols
        val headerHeight = 220
        val footerHeight = 80
        val gridHeight = rows * cellSize + padding
        val totalHeight = headerHeight + gridHeight + footerHeight

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E65100")
            textSize = 52f
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#555555")
            textSize = 34f
        }
        val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#999999")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }

        // Header
        canvas.drawText(raffle.name, padding.toFloat(), 70f, titlePaint)
        canvas.drawText(
            "Rango: ${raffle.minNumber}–${raffle.maxNumber}",
            padding.toFloat(), 115f, subtitlePaint
        )
        val freeCount = range - assigned.size
        canvas.drawText(
            "${assigned.size} ocupados · $freeCount libres",
            padding.toFloat(), 160f, subtitlePaint
        )

        // Divider
        val dividerPaint = Paint().apply { color = Color.parseColor("#EEEEEE") }
        canvas.drawRect(padding.toFloat(), 180f, (width - padding).toFloat(), 185f, dividerPaint)

        // Number grid
        for (i in 0 until range) {
            val number = raffle.minNumber + i
            val col = i % cols
            val row = i / cols
            val left = (padding + col * cellSize + 2).toFloat()
            val top = (headerHeight + row * cellSize + 2).toFloat()
            val right = left + cellSize - 4
            val bottom = top + cellSize - 4

            cellPaint.color = if (number in assigned) Color.parseColor("#E65100") else Color.parseColor("#C8E6C9")
            canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, cellPaint)

            numberPaint.color = if (number in assigned) Color.WHITE else Color.parseColor("#2E7D32")
            canvas.drawText(
                number.toString(),
                left + (cellSize - 4) / 2f,
                top + (cellSize - 4) / 2f + 8f,
                numberPaint
            )
        }

        // Footer
        canvas.drawText(
            "Generado con EasyRifa",
            width / 2f,
            (totalHeight - 20).toFloat(),
            footerPaint
        )

        return bitmap
    }

    private fun saveBitmapAndGetUri(bitmap: Bitmap, raffleId: Long): Uri {
        val dir = File(context.cacheDir, "shared").also { it.mkdirs() }
        val file = File(dir, "raffle_status_$raffleId.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}

package com.easyrifa.domain.usecase.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import com.easyrifa.data.db.entity.RaffleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Generates a shareable PNG image showing the current raffle status:
 * prize image (if any), number grid colored by assignment state, and counters.
 *
 * Layout order: name (top, wrapped) → prize photo → stats/price → number grid → footer
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
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /** Breaks [text] into lines that fit within [maxWidth] pixels using [paint] metrics. */
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        var current = ""
        for (word in text.split(" ")) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = candidate
            } else {
                if (current.isNotEmpty()) lines.add(current)
                current = word
            }
        }
        if (current.isNotEmpty()) lines.add(current)
        return lines
    }

    private fun loadOrientedBitmap(path: String): Bitmap? {
        val raw = BitmapFactory.decodeFile(path) ?: return null
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90       -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180      -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270      -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL   -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE    -> { matrix.postRotate(90f);  matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE   -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
        }
        return if (matrix.isIdentity) raw
        else Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
            .also { if (it !== raw) raw.recycle() }
    }

    private fun generateBitmap(data: RaffleShareData): Bitmap {
        val raffle = data.raffle
        val assigned = data.assignedNumbers
        val range = raffle.maxNumber - raffle.minNumber + 1

        // Paints — defined early so we can measure text for layout
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
        val dividerPaint = Paint().apply { color = Color.parseColor("#EEEEEE") }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#999999")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }

        // Load prize image with EXIF orientation correction
        val prizeBitmap: Bitmap? = raffle.imagePath
            ?.takeIf { File(it).exists() }
            ?.let { loadOrientedBitmap(it) }

        // ── Layout measurements ────────────────────────────────────────
        val width = 1080
        val padding = 40
        val cellSize = 60
        val cols = (width - padding * 2) / cellSize
        val rows = (range + cols - 1) / cols
        val titleLineHeight = 64f

        // 1. Name section: multi-line title at the top
        val nameLines = wrapText(raffle.name, titlePaint, (width - padding * 2).toFloat())
        val nameSectionHeight = (padding + nameLines.size * titleLineHeight + padding / 2f).toInt()

        // 2. Prize image: fit-inside capped at 16:9
        val prizeImageHeight = if (prizeBitmap != null) {
            val naturalH = (prizeBitmap.height.toFloat() / prizeBitmap.width * width).toInt()
            minOf(naturalH, width * 9 / 16)
        } else 0

        // 3. Stats section: range + occupied/free + optional price + optional draw date + divider
        val statsLineCount = 2 +
            (if (raffle.pricePerNumber != null) 1 else 0) +
            (if (raffle.drawDate != null) 1 else 0)
        val statsSectionHeight = padding / 2 + statsLineCount * 44 + padding / 2 + 25 // lines + divider gap

        // 4. Grid + footer
        val gridHeight = rows * cellSize + padding
        val footerHeight = 80
        val totalHeight = nameSectionHeight + prizeImageHeight + statsSectionHeight + gridHeight + footerHeight

        // ── Canvas ─────────────────────────────────────────────────────
        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        // ── 1. Name (top) ──────────────────────────────────────────────
        var y = padding + titleLineHeight
        for (line in nameLines) {
            canvas.drawText(line, padding.toFloat(), y, titlePaint)
            y += titleLineHeight
        }

        // ── 2. Prize image (middle) ────────────────────────────────────
        if (prizeBitmap != null) {
            val scale = minOf(
                width.toFloat() / prizeBitmap.width,
                prizeImageHeight.toFloat() / prizeBitmap.height
            )
            val drawnW = prizeBitmap.width * scale
            val drawnH = prizeBitmap.height * scale
            val offsetX = (width - drawnW) / 2f
            val offsetY = nameSectionHeight + (prizeImageHeight - drawnH) / 2f
            val dst = RectF(offsetX, offsetY, offsetX + drawnW, offsetY + drawnH)
            canvas.drawBitmap(prizeBitmap, null, dst, Paint(Paint.ANTI_ALIAS_FLAG))
            prizeBitmap.recycle()
        }

        // ── 3. Stats (below image) ─────────────────────────────────────
        val freeCount = range - assigned.size
        var statsY = nameSectionHeight + prizeImageHeight + padding / 2f + 44f
        canvas.drawText(
            "Rango: ${raffle.minNumber}–${raffle.maxNumber}",
            padding.toFloat(), statsY, subtitlePaint
        )
        statsY += 44f
        canvas.drawText(
            "${assigned.size} ocupados · $freeCount libres",
            padding.toFloat(), statsY, subtitlePaint
        )
        if (raffle.pricePerNumber != null) {
            statsY += 44f
            val priceFormatted = String.format(Locale.getDefault(), "%.2f", raffle.pricePerNumber)
            canvas.drawText(
                "Precio por número: $priceFormatted €",
                padding.toFloat(), statsY, subtitlePaint
            )
        }
        if (raffle.drawDate != null) {
            statsY += 44f
            val dateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(raffle.drawDate))
            canvas.drawText(
                "Fecha del sorteo: $dateFormatted",
                padding.toFloat(), statsY, subtitlePaint
            )
        }
        val dividerTop = nameSectionHeight + prizeImageHeight + statsSectionHeight - 20f
        canvas.drawRect(padding.toFloat(), dividerTop, (width - padding).toFloat(), dividerTop + 5f, dividerPaint)

        // ── 4. Number grid ─────────────────────────────────────────────
        val gridTop = nameSectionHeight + prizeImageHeight + statsSectionHeight
        for (i in 0 until range) {
            val number = raffle.minNumber + i
            val col = i % cols
            val row = i / cols
            val left   = (padding + col * cellSize + 2).toFloat()
            val top    = (gridTop + row * cellSize + 2).toFloat()
            val right  = left + cellSize - 4
            val bottom = top  + cellSize - 4

            cellPaint.color = if (number in assigned) Color.parseColor("#E65100") else Color.parseColor("#C8E6C9")
            canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, cellPaint)

            numberPaint.color = if (number in assigned) Color.WHITE else Color.parseColor("#2E7D32")
            canvas.drawText(
                number.toString(),
                left + (cellSize - 4) / 2f,
                top  + (cellSize - 4) / 2f + 8f,
                numberPaint
            )
        }

        // ── 5. Footer ──────────────────────────────────────────────────
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

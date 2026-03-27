package com.easyrifa.domain.usecase.share

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * Copies an image URI (picked from gallery) into the app's internal storage
 * so it remains accessible even if the original URI becomes invalid.
 * Returns the path to the internal copy.
 */
class CopyRaffleImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun execute(sourceUri: Uri): Result<String> = runCatching {
        val dir = File(context.filesDir, "raffle_images").also { it.mkdirs() }
        val destFile = File(dir, "${UUID.randomUUID()}.jpg")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("No se pudo abrir la imagen seleccionada")

        destFile.absolutePath
    }

    fun deleteImage(path: String) {
        runCatching { File(path).delete() }
    }
}

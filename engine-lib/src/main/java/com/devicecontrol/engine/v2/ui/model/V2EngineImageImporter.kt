package com.devicecontrol.engine.v2.ui.model

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.IOException

/** 将相册选择的图片复制到应用私有目录，返回可持久化的本地路径。 */
object V2EngineImageImporter {

    private const val STORAGE_DIR = "engine_model_images"

    fun copyToAppStorage(context: Context, uri: Uri): String? {
        return try {
            val ext = resolveExtension(context, uri)
            val dir = File(context.filesDir, STORAGE_DIR).apply { mkdirs() }
            val dest = File(dir, "model_${System.currentTimeMillis()}.$ext")
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            if (!dest.exists() || dest.length() <= 0L) {
                dest.delete()
                return null
            }
            dest.absolutePath
        } catch (_: IOException) {
            null
        }
    }

    private fun resolveExtension(context: Context, uri: Uri): String {
        val mime = context.contentResolver.getType(uri)
        val fromMime = mime?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
        if (!fromMime.isNullOrBlank()) return fromMime
        val path = uri.lastPathSegment.orEmpty()
        val dot = path.lastIndexOf('.')
        if (dot >= 0 && dot < path.length - 1) {
            return path.substring(dot + 1).lowercase()
        }
        return "jpg"
    }
}

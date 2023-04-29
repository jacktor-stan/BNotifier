package com.jacktorscript.batterynotifier.core

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Toast
import com.jacktorscript.batterynotifier.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


object FileUtils {
    fun getPath(context: Context, uri: Uri): String? {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract
                    .getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/" + split[1]
                }

                // Handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri
                        .parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)!!
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract
                    .getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(
                    context, contentUri, selection,
                    selectionArgs
                )!!
            }
        }

        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri
            .authority
    }


    private fun getDataColumn(
        context: Context, uri: Uri?,
        selection: String?, selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(
                uri!!, projection,
                selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex: Int = cursor
                    .getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri
            .authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri
            .authority
    }

    fun getFileName(uri: Uri?): String? {
        if (uri == null) return null
        var fileName: String? = null
        val path: String? = uri.path
        val cut = path?.lastIndexOf('/')
        if (cut != -1) {
            fileName = path!!.substring(cut!! + 1)
        }
        return fileName
    }

    /*
    fun copy(src: Uri, dst: Uri) {
        val `in`: InputStream = FileInputStream(File(src.path!!))
        `in`.use {
            val out: OutputStream = FileOutputStream(File(dst.path!!))
            out.use {
                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
    }
     */

    fun saveFile(context: Context, path: Uri, filename: String) {
        val inFile = File(path.path!!)
        val outFile = File(context.filesDir, filename)
        val inStream = FileInputStream(inFile)
        val outStream = FileOutputStream(outFile)
        val buffer = ByteArray(65536)
        var len: Int
        while (inStream.read(buffer).also { len = it } != -1) {
            outStream.write(buffer, 0, len)
        }
        inStream.close()
        outStream.close()
    }


    fun removeFile(context: Context, showMsg: Boolean, filename: String) {
        val file = File(context.filesDir, filename)
        if (file.exists()) {
            val file2 = File(file.absolutePath)
            file2.delete()
            if (showMsg)
            Toast.makeText(context, context.getString(R.string.reset_audio_success), Toast.LENGTH_SHORT).show()
        } else {
            if (showMsg)
            Toast.makeText(context, context.getString(R.string.reset_audio_failed), Toast.LENGTH_SHORT).show()
        }
    }

}
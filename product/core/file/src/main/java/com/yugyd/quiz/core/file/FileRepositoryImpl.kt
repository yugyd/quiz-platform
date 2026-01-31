package com.yugyd.quiz.core.file

import android.content.Context
import android.database.Cursor
import android.net.Uri.parse
import android.provider.OpenableColumns
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FileRepository {

    override fun saveTextToLocalStorage(fileName: String, fileContents: String): File {
        val file = File(context.filesDir, fileName)

        if (!file.exists()) {
            file.createNewFile()
        }

        file.outputStream().use {
            it.write(fileContents.toByteArray())
        }

        return file
    }

    /**
     * https://developer.android.com/training/secure-file-sharing/retrieve-info#RetrieveFileInfo
     */
    override fun getFileName(uri: String): String? {
        val contentResolver = context.contentResolver

        val androidUri = uri.toUri()
        val cursor: Cursor? = contentResolver.query(androidUri, null, null, null, null, null)
        val fileName = cursor?.use {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            val isMove = cursor.moveToFirst()

            if (isMove) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }

        return fileName
    }

    override fun checkUri(uri: String): Boolean {
        val contentResolver = context.contentResolver

        val androidUri = uri.toUri()
        val cursor: Cursor? = contentResolver.query(androidUri, null, null, null, null, null)
        val result = cursor?.use {
            it.count > 1
        }
        return result ?: false
    }

    override fun readTextFromUri(uri: String): String {
        val contentResolver = context.contentResolver

        val stringBuilder = StringBuilder()

        val androidUri = uri.toUri()
        contentResolver.openInputStream(androidUri)?.use { inputStream ->
            val inputStreamReader = InputStreamReader(inputStream)
            BufferedReader(inputStreamReader).use { reader ->
                stringBuilder.append(reader.readText())
            }
        }

        return stringBuilder.toString()
    }

    override fun readTextFromFile(fileName: String): String {
        val file = File(fileName)

        if (!file.exists()) {
            throw FileNotFoundException("$fileName not founded")
        }

        val stringBuilder = StringBuilder()

        file.inputStream().use { inputStream ->
            val inputStreamReader = InputStreamReader(inputStream)
            BufferedReader(inputStreamReader).use { reader ->
                stringBuilder.append(reader.readText())
            }
        }

        return stringBuilder.toString()
    }
}

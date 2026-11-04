package com.shrikantbadwaik.landingkit.core.data.local.attachment

import android.content.ContentProvider
import android.content.ContentValues
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File

/** Non-exported test APK provider, restricted to the synthetic source PDF in a fixture. */
class MutationEvidenceProvider : ContentProvider() {
    override fun onCreate() = true
    override fun getType(uri: Uri) = "application/pdf"
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?) =
        MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)).apply {
            val source = source(uri)
            addRow(arrayOf("synthetic.pdf", source.length()))
        }
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        require(mode == "r")
        return ParcelFileDescriptor.open(source(uri), ParcelFileDescriptor.MODE_READ_ONLY)
    }
    private fun source(uri: Uri): File {
        val file = File(checkNotNull(uri.getQueryParameter("file"))).canonicalFile
        val root = checkNotNull(context).filesDir.canonicalFile
        require(file.name == "source.pdf" && file.parentFile?.parentFile == root)
        require(file.parentFile!!.name.startsWith("mutation-test-") ||
            file.parentFile!!.name == "mutation-process-probe")
        return file
    }
    override fun insert(uri: Uri, values: ContentValues?): Uri? = error("Read only")
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<out String>?) = 0
}

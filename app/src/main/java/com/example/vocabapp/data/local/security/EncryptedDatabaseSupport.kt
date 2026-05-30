package com.example.vocabapp.data.local.security

import android.content.Context
import android.util.Log
import java.io.File
import java.nio.charset.StandardCharsets
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

internal object EncryptedDatabaseSupport {
    const val DATABASE_NAME = "toeic_vocab.db"

    fun createSupportFactory(context: Context): SupportFactory {
        SQLiteDatabase.loadLibs(context)
        val passphrase = DatabasePassphraseProvider(context).getOrCreatePassphrase()
        migratePlaintextDatabaseIfNeeded(context, passphrase)
        return SupportFactory(passphrase, null, true)
    }

    private fun migratePlaintextDatabaseIfNeeded(context: Context, passphrase: ByteArray) {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        if (!databaseFile.exists() || !databaseFile.isPlaintextSqlite()) return

        databaseFile.parentFile?.mkdirs()
        val encryptedFile = File(databaseFile.parentFile, "$DATABASE_NAME.encrypted")
        if (encryptedFile.exists()) encryptedFile.delete()

        try {
            val plaintextDatabase = SQLiteDatabase.openDatabase(
                databaseFile.absolutePath,
                "",
                null,
                SQLiteDatabase.OPEN_READWRITE
            )
            try {
                plaintextDatabase.rawExecSQL("PRAGMA wal_checkpoint(FULL)")
                val key = passphrase.toString(StandardCharsets.UTF_8).toSqlLiteral()
                val targetPath = encryptedFile.absolutePath.toSqlLiteral()
                plaintextDatabase.rawExecSQL("ATTACH DATABASE $targetPath AS encrypted KEY $key")
                plaintextDatabase.rawExecSQL("SELECT sqlcipher_export('encrypted')")
                plaintextDatabase.rawExecSQL("DETACH DATABASE encrypted")
            } finally {
                plaintextDatabase.close()
            }

            val backupFile = File(databaseFile.parentFile, "$DATABASE_NAME.plaintext-backup")
            if (backupFile.exists()) backupFile.delete()
            check(databaseFile.renameTo(backupFile)) { "Failed to back up plaintext database" }
            try {
                check(encryptedFile.renameTo(databaseFile)) { "Failed to install encrypted database" }
                backupFile.delete()
            } catch (error: Throwable) {
                backupFile.renameTo(databaseFile)
                encryptedFile.delete()
                throw error
            }
            databaseFile.sibling("-wal").delete()
            databaseFile.sibling("-shm").delete()
        } catch (error: Exception) {
            Log.e(TAG, "Plaintext migration failed — discarding old database to allow fresh start", error)
            encryptedFile.delete()
            databaseFile.delete()
            databaseFile.sibling("-wal").delete()
            databaseFile.sibling("-shm").delete()
        }
    }

    private fun File.isPlaintextSqlite(): Boolean {
        if (!exists() || length() < SQLITE_HEADER.size) return false
        return inputStream().use { input ->
            val header = ByteArray(SQLITE_HEADER.size)
            input.read(header) == SQLITE_HEADER.size && header.contentEquals(SQLITE_HEADER)
        }
    }

    private fun File.sibling(suffix: String): File =
        File(parentFile, "$name$suffix")

    private fun String.toSqlLiteral(): String =
        "'${replace("'", "''")}'"

    private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(StandardCharsets.US_ASCII)
    private const val TAG = "EncryptedDatabaseSupport"
}

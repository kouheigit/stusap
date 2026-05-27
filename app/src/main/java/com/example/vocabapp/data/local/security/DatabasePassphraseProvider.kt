package com.example.vocabapp.data.local.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class DatabasePassphraseProvider(
    context: Context
) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getOrCreatePassphrase(): ByteArray {
        val encrypted = preferences.getString(KEY_ENCRYPTED_PASSPHRASE, null)
        if (encrypted != null) return decrypt(encrypted)

        val randomBytes = ByteArray(DATABASE_PASSPHRASE_BYTES)
        SecureRandom().nextBytes(randomBytes)
        val passphrase = randomBytes.toBase64().toByteArray(StandardCharsets.UTF_8)
        preferences.edit()
            .putString(KEY_ENCRYPTED_PASSPHRASE, encrypt(passphrase))
            .apply()
        return passphrase
    }

    private fun encrypt(plain: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val cipherText = cipher.doFinal(plain)
        return "${cipher.iv.toBase64()}:${cipherText.toBase64()}"
    }

    private fun decrypt(encoded: String): ByteArray {
        val parts = encoded.split(":")
        require(parts.size == 2) { "Invalid database passphrase payload" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(GCM_TAG_BITS, parts[0].fromBase64())
        )
        return cipher.doFinal(parts[1].fromBase64())
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun ByteArray.toBase64(): String =
        Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.fromBase64(): ByteArray =
        Base64.decode(this, Base64.NO_WRAP)

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val DATABASE_PASSPHRASE_BYTES = 32
        const val GCM_TAG_BITS = 128
        const val KEY_ALIAS = "vocabapp_room_database_key"
        const val KEY_ENCRYPTED_PASSPHRASE = "encrypted_room_passphrase"
        const val PREFS_NAME = "database_security"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}

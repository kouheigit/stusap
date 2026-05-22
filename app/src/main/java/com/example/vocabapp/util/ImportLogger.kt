package com.example.vocabapp.util

import android.util.Log
import com.example.vocabapp.BuildConfig

private const val IMPORT_TAG = "ExcelImport"
private val canUseAndroidLog: Boolean =
    System.getProperty("java.vm.name").orEmpty().contains("Dalvik", ignoreCase = true)

internal fun debugImportLog(message: String) {
    if (BuildConfig.DEBUG && canUseAndroidLog) Log.d(IMPORT_TAG, message)
}

internal fun warnImportLog(message: String) {
    if (BuildConfig.DEBUG && canUseAndroidLog) Log.w(IMPORT_TAG, message)
}

internal fun errorImportLog(message: String, throwable: Throwable? = null) {
    if (BuildConfig.DEBUG && canUseAndroidLog) {
        if (throwable == null) Log.e(IMPORT_TAG, message) else Log.e(IMPORT_TAG, message, throwable)
    }
}

package com.example.vocabapp

import android.util.Log

private const val IMPORT_TAG = "ExcelImport"

internal fun debugImportLog(message: String) {
    if (BuildConfig.DEBUG) Log.d(IMPORT_TAG, message)
}

internal fun warnImportLog(message: String) {
    if (BuildConfig.DEBUG) Log.w(IMPORT_TAG, message)
}

internal fun errorImportLog(message: String, throwable: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        if (throwable == null) Log.e(IMPORT_TAG, message) else Log.e(IMPORT_TAG, message, throwable)
    }
}

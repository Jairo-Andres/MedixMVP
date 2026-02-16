package com.medix.mvpmvp.core.storage

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class JsonStorage(context: Context) {
    private val prefs = context.getSharedPreferences("medix_storage", Context.MODE_PRIVATE)

    fun putArray(key: String, values: JSONArray) {
        prefs.edit().putString(key, values.toString()).apply()
    }

    fun getArray(key: String): JSONArray? {
        val raw = prefs.getString(key, null) ?: return null
        return runCatching { JSONArray(raw) }.getOrNull()
    }

    fun putObject(key: String, value: JSONObject) {
        prefs.edit().putString(key, value.toString()).apply()
    }

    fun getObject(key: String): JSONObject? {
        val raw = prefs.getString(key, null) ?: return null
        return runCatching { JSONObject(raw) }.getOrNull()
    }

    fun contains(key: String): Boolean = prefs.contains(key)
}

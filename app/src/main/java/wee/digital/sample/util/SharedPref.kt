package wee.digital.sample.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import wee.digital.sample.app

/**
 * Add to manifest:
 * android:allowBackup="false"
 * android:fullBackupContent="false"
 */
class SharedPref(private val fileName: String) {

    private val pref: SharedPreferences by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                fileName,
                masterKey,
                app,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } else {
            app.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        }
    }

    fun edit(block: SharedPreferences.Editor.() -> Unit): Boolean {
        val edit = pref.edit()
        edit.block()
        return edit.commit()
    }

    fun str(key: String, default: String? = null): String? = pref.getString(key, default)

    fun long(key: String, default: Long = -1): Long = pref.getLong(key, -default)

    fun int(key: String, default: Int = -1): Int = pref.getInt(key, default)

    fun bool(key: String, default: Boolean = false): Boolean = pref.getBoolean(key, default)

    inline fun <reified T : Any> obj(key: String): T? {
        return str(key).toJsonObject().parse<T>()
    }

    fun float(key: String, default: Float = -1F): Float = pref.getFloat(key, default)

    fun put(key: String, value: String?): Boolean {
        return edit { putString(key, value) }
    }

    fun put(key: String, value: Long): Boolean {
        return edit { putLong(key, value) }
    }

    fun put(key: String, value: Int): Boolean {
        return edit { putInt(key, value) }
    }

    fun put(key: String, value: Boolean): Boolean {
        return edit { putBoolean(key, value) }
    }

    fun put(key: String, value: Float): Boolean {
        return edit { putFloat(key, value) }
    }

    fun <T : Any> putObj(key: String, value: T?) {
        edit { putString(key, value.toJsonObject().toString()) }
    }

}
package engineer.yusrisahrul.chatapp.util

import android.content.Context
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_PREFERENCE_NAME

class PreferenceManager (context: Context) {
    private val preference = context.getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val editor = preference.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String) : Boolean{
        return preference.getBoolean(key, false)
    }

    fun putString(key: String, value: String) {
        val editor = preference.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String) : String? {
        return preference.getString(key, "")
    }

    fun clear() {
        val editor = preference.edit()
        editor.clear()
        editor.apply()
    }
}
package engineer.yusrisahrul.chatapp.util

import android.content.Context
import engineer.yusrisahrul.chatapp.util.Constants.Companion.KEY_PREFERENCE_NAME

class PreferenceManager (private var context: Context) {
    val isShift = "IS_SHIFT"
    val id_shift = "ID_SHIFT"
    val tgl_shift = "TGL_SHIFT"
    val mulai_shift = "MULAI_SHIFT"
    val saldo_awal = "SALDO_AWAL"
    val id_bluethoot = "ID_BLUETHOOT"
    val DAYS = "DAYS"
    val PPN = "PPN"
    val id_shift_from_history = "ID_SHIFT_FROM_HISTORY"
    val sw_ppn = "RADIO_PPN"
    val sw_pb1 = "RADIO_PB1"
    val sw_noppn = "TANPA PPN"
    val nama_outlet = "NAMA_OTULET"
    val header1 = "HEADER_1"
    val header2 = "HEADER_2"
    val header3 = "HEADER_3"
    val sw_header = "SWITCH_HEADER"
    val TODAY = "TODAY"
    val URLIMAGE = "URLIMAGE"
    val preference = context.getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)

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
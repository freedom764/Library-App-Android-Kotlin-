package edu.skku.cs.final_library

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager


object SaveSharedPreference {
    const val PREF_USER_NAME = "username"
    const val PREF_LAST = "last"
    fun getSharedPreferences(ctx: Context?): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
    }

    fun setUserName(ctx: Context?, userName: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(PREF_USER_NAME, userName)
        editor.commit()
    }

    fun getUserName(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "")
    }


    fun setLast(ctx: Context?, last: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(PREF_LAST, last)
        editor.commit()
    }

    fun getLast(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(PREF_LAST, "")
    }

}
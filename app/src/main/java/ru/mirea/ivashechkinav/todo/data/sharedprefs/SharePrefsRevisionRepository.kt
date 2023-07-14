package ru.mirea.ivashechkinav.todo.data.sharedprefs

import android.content.Context
import android.content.SharedPreferences
import ru.mirea.ivashechkinav.todo.di.components.AppScope
import javax.inject.Inject

@AppScope
class SharePrefsRevisionRepository @Inject constructor(
    private val prefs: SharedPreferences
)  {

    fun getLastRevision(): Int {
        return prefs.getInt(KEY_REVISION, 0)
    }

    fun setRevision(revision: Int) {
        prefs.edit().putInt(KEY_REVISION, revision).apply()
    }

    companion object {
        private const val KEY_REVISION = "revision"
    }
}

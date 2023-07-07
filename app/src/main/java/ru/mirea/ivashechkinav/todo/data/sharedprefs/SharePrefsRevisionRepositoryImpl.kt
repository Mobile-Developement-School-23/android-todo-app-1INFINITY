package ru.mirea.ivashechkinav.todo.data.sharedprefs

import android.content.Context
import android.content.SharedPreferences
import ru.mirea.ivashechkinav.todo.di.components.AppContext
import javax.inject.Inject

class SharePrefsRevisionRepositoryImpl @Inject constructor(@AppContext private val applicationContext: Context): RevisionRepository {

    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    override fun getLastRevision(): Int {
        return sharedPreferences.getInt(KEY_REVISION, 0)
    }

    override fun setRevision(revision: Int) {
        sharedPreferences.edit().putInt(KEY_REVISION, revision).apply()
    }

    override fun hasLocalChanges(): Boolean {
        return sharedPreferences.getBoolean(KEY_CHANGE, false)
    }

    override fun editLocalChanges(isSomethingChanged: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_CHANGE, isSomethingChanged).apply()
    }

    companion object {
        private const val PREF_NAME = "RevisionPrefs"
        private const val KEY_REVISION = "revision"
        private const val KEY_CHANGE = "change"
    }
}

package ru.mirea.ivashechkinav.todo.data.sharedprefs

import android.content.Context
import android.content.SharedPreferences
import ru.mirea.ivashechkinav.todo.di.components.AppContext
import ru.mirea.ivashechkinav.todo.di.components.AppScope
import javax.inject.Inject

@AppScope
class SharePrefsRevisionRepositoryImpl @Inject constructor(
    @AppContext private val applicationContext: Context
) : RevisionRepository {

    private val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    override fun getLastRevision(): Int {
        return sharedPreferences.getInt(KEY_REVISION, 0)
    }

    override fun setRevision(revision: Int) {
        sharedPreferences.edit().putInt(KEY_REVISION, revision).apply()
    }

    companion object {
        private const val PREF_NAME = "RevisionPrefs"
        private const val KEY_REVISION = "revision"
    }
}

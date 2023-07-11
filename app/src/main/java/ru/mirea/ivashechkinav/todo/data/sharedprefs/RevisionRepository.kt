package ru.mirea.ivashechkinav.todo.data.sharedprefs

interface RevisionRepository {

    fun getLastRevision(): Int

    fun setRevision(revision: Int)
}
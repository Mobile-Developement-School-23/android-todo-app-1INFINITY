package ru.mirea.ivashechkinav.todo.data.sharedprefs

interface RevisionRepository { // good to make it typed, but single impl interfaces are unnecessary

    fun getLastRevision(): Int

    fun setRevision(revision: Int)
}
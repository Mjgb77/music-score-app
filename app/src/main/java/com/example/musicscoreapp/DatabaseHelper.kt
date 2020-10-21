package com.example.musicscoreapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_MUSIC_SCORES ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TITLE TEXT);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MUSIC_SCORES")
        onCreate(db)
    }

    fun addScore(title: String){
        val db =  this.writableDatabase;
        val cv =  ContentValues();

        cv.put(COLUMN_TITLE, title);
      val result =  db.insert(TABLE_MUSIC_SCORES, null, cv);

        if(result == -1L){
            Toast.makeText(context,  "Insertion failed", Toast.LENGTH_SHORT).show()
        }else {
            Toast.makeText(context, "$result $title added", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        private const val DATABASE_NAME = "MusicScores.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_MUSIC_SCORES = "music_scores"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_TITLE = "title"
    }
}
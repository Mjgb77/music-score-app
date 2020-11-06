package com.example.musicscoreapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class AddScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_score)

        titleInput = findViewById(R.id.title_input)
        addButton = findViewById(R.id.add_button)

        addButton?.setOnClickListener { _ ->
            val title = titleInput?.text.toString().trim()

            if(title.isEmpty()){
                titleInput?.error = "Title should not be blank"
                return@setOnClickListener
            }

            val fileStorageHelper = FileStorageHelper(this)
            if(!fileStorageHelper.addScore(title)){
                titleInput?.error = "Couldn't create the song, check if a song with this name already exists"
                return@setOnClickListener
            }
            finish()
        }

        findViewById<Button>(R.id.camera_button)?.setOnClickListener {_ ->
            val intent = Intent(this, DetectorActivity::class.java)
            startActivity(intent)
        }
    }


    companion object {
        private var titleInput : EditText? = null
        private var addButton: Button? = null
    }
}
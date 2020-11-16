package com.example.musicscoreapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.opensooq.supernova.gligar.GligarPicker

class AddScoreActivity : AppCompatActivity() {
    private var titleInput : EditText? = null
    private var addButton: Button? = null
    private var selectImageButton: Button? = null
    private var selectedImages: Array<String>? = null

    private val detectionHelper = DetectionHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_score)

        titleInput = findViewById(R.id.title_input)
        addButton = findViewById(R.id.add_button)
        selectImageButton = findViewById(R.id.select_images_button)

        addButton?.setOnClickListener { _ ->
            val title = titleInput?.text.toString().trim()

            if(title.isEmpty()){
                titleInput?.error = "Title should not be blank"
                return@setOnClickListener
            }

            if(selectedImages == null || selectedImages!!.isEmpty()){
                //TODO show this error using another way
                selectImageButton?.error = "Images must be selected before continue"
                return@setOnClickListener
            }

            Toast.makeText(this,"Generating Midi file... Please wait", Toast.LENGTH_LONG).show()
            detectionHelper.readMidi (selectedImages!!) { s ->
                run {
                    val fileStorageHelper = FileStorageHelper(this)
                    if (!fileStorageHelper.addScore(title, selectedImages!!, s)) {
                        titleInput?.error = "Couldn't create the song, check if a song with this name already exists"
                        return@run
                    }
                    finish()
                }
            }
        }

        selectImageButton?.setOnClickListener {
            _ ->
            GligarPicker()
                    .requestCode(PICKER_REQUEST_CODE)
                    .withActivity(this)
                    .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode != Activity.RESULT_OK) return

        when (requestCode){
            PICKER_REQUEST_CODE -> {
                selectedImages = data?.extras?.getStringArray(GligarPicker.IMAGES_RESULT)
                if(selectedImages!= null && selectedImages!!.isNotEmpty())
                    selectImageButton?.error = null
            }
        }

    }

    companion object {
        private const val PICKER_REQUEST_CODE = 1
    }
}
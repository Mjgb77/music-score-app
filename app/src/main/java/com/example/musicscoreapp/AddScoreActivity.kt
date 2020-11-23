package com.example.musicscoreapp

import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicscoreapp.adapter.NoFilterArrayAdapter
import com.example.musicscoreapp.adapter.SheetAdapter
import com.example.musicscoreapp.databinding.ActivityAddScoreBinding
import com.example.musicscoreapp.service.PictureService
import com.example.musicscoreapp.service.ScoreToMidiConverter
import kotlinx.coroutines.launch


class AddScoreActivity : AppCompatActivity() {
    private lateinit var addButton: Button
    private lateinit var createButton: Button

    private val pictureService = PictureService(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_score)

        val scoreModel: AddScoreViewModel by viewModels()
        val binding: ActivityAddScoreBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_add_score
        )
        binding.viewmodel = scoreModel


        addButton = findViewById(R.id.buttonAddImage)
        createButton = findViewById(R.id.fabCreate)

        val adapter = NoFilterArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_instrument,
            scoreModel.instruments.keys.toTypedArray()
        );

        val editTextFilledExposedDropdown: AutoCompleteTextView =
            findViewById(R.id.autoCompleteInstrument)
        editTextFilledExposedDropdown.setAdapter(adapter)

        val sheetAdapter = SheetAdapter(scoreModel)
        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val recyclerViewImages: RecyclerView = findViewById(R.id.recyclerViewImages)
        recyclerViewImages.adapter = sheetAdapter
        recyclerViewImages.layoutManager = layoutManager

        addButton.setOnClickListener {
            pictureService.takePicture { imageFile ->
                lifecycleScope.launch {
                    val image = pictureService.readBitmap(imageFile)
                    sheetAdapter.add(image, imageFile)
                    recyclerViewImages.scrollToPosition(sheetAdapter.itemCount - 1)
                }
            }
        }

        createButton.setOnClickListener {
            scoreModel.setInstrumentName(editTextFilledExposedDropdown.text.toString())
            scoreModel.createScore()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pictureService.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        pictureService.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
package com.evmg.musicscoreapp.activities

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
import com.evmg.musicscoreapp.viewmodels.AddScoreViewModel
import com.evmg.musicscoreapp.R
import com.evmg.musicscoreapp.adapter.NoFilterArrayAdapter
import com.evmg.musicscoreapp.adapter.SheetAdapter
import com.evmg.musicscoreapp.databinding.ActivityCreateOrUpdateScoreBinding
import com.evmg.musicscoreapp.model.Score
import com.evmg.musicscoreapp.service.PictureService
import com.evmg.musicscoreapp.service.ScoreDb
import kotlinx.coroutines.launch


class CreateOrUpdateScoreActivity : AppCompatActivity() {
    private lateinit var addButton: Button
    private lateinit var createButton: Button
    private lateinit var scoreDb: ScoreDb
    private var initialized = false

    companion object {
        val SCORE_PATH = "scorePath"
    }

    private val pictureService = PictureService(this)
    //cancion a -> view -> edit (tempCancionA) -> view cancionA <- tempCancionA
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_or_update_score)
        scoreDb = ScoreDb(this)

        val scoreModel: AddScoreViewModel by viewModels()
        val binding: ActivityCreateOrUpdateScoreBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_create_or_update_score
        )
        binding.viewmodel = scoreModel

        if (!initialized) {
            initialized = true
            getScoreFromIntent()?.also { scoreModel.setOriginalScore(it) }
        }

        addButton = findViewById(R.id.buttonAddImage)
        createButton = findViewById(R.id.fabCreate)

        val adapter = NoFilterArrayAdapter(
            this,
            R.layout.dropdown_menu_popup_instrument,
            AddScoreViewModel.INSTRUMENTS_BY_NAME.keys.toTypedArray()
        )

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
            pictureService.takePicture(scoreModel.getNewImageFile()) { imageFile ->
                lifecycleScope.launch {
                    sheetAdapter.add(imageFile)
                    recyclerViewImages.scrollToPosition(sheetAdapter.itemCount - 1)
                }
            }
        }

        createButton.setOnClickListener {
            scoreModel.createScore()
            finish()
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

    private fun getScoreFromIntent(): Score? {
        if (intent.hasExtra(ScoreDetailsActivity.SCORE_PATH)) {
            return scoreDb.getScore(intent.getStringExtra(ScoreDetailsActivity.SCORE_PATH)!!)
        }
        return null
    }
}
/*
Temp/generado/(imagene y to la cosa)
Score/(muchodir uno por cancion)

 */
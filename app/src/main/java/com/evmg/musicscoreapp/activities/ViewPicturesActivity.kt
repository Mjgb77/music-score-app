package com.evmg.musicscoreapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evmg.musicscoreapp.R
import com.evmg.musicscoreapp.adapter.ViewSheetAdapter
import com.evmg.musicscoreapp.service.ScoreDb

class ViewPicturesActivity : AppCompatActivity() {
    val scoreDb = ScoreDb(this)

    companion object {
        val SCORE_DIR = "scoreDir"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pictures)



        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewImages)
        recyclerView.adapter = ViewSheetAdapter(scoreDb.getScore(intent.getStringExtra(
            SCORE_DIR
        )!!)!!.sheets!!)
        recyclerView.layoutManager = layoutManager
    }
}
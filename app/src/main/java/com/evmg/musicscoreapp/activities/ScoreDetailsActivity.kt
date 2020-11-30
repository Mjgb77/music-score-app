package com.evmg.musicscoreapp.activities

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.evmg.musicscoreapp.R
import com.evmg.musicscoreapp.model.Score
import com.evmg.musicscoreapp.service.ScoreDb
import com.evmg.musicscoreapp.viewmodels.AddScoreViewModel
import java.io.File
import java.text.DateFormat
import java.util.*

class ScoreDetailsActivity : AppCompatActivity() {
    companion object {
        val SCORE_PATH = "scorePath"
        val TYPE = "type"
        val TYPE_IMPORT = "import"
    }

    private val scoreDb = ScoreDb(this)
    private lateinit var score: Score

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_details)
        val result = getScoreFromIntent()

        if (result == null) {
            Toast.makeText(this, "Score not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        score = result

        val txtScoreTitle: TextView = findViewById(R.id.textScoreTitle)
        val textDate: TextView = findViewById(R.id.textDate)
        val textTempo: TextView = findViewById(R.id.textTempo)
        val textInstrument: TextView = findViewById(R.id.textInstrument)
        val btnPlay: Button = findViewById(R.id.btnPlay)
        val btnViewImages: Button = findViewById(R.id.btnViewImages)

        txtScoreTitle.text = score.title
        textDate.text = DateFormat.getDateTimeInstance().format(Date(score.createDate))
        textTempo.text = score.tempo.toString()
        textInstrument.text = AddScoreViewModel.INSTRUMENTS_BY_ID[score.instrument] ?: score.instrument.toString()

        btnPlay.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW

            //TODO use dynamically generated audio file name
            val contentUri: Uri = FileProvider.getUriForFile(
                this,
                "com.evmg.musicscoreapp.fileprovider",
                File(score.dir, "audio.mid")
            )

            intent.setDataAndType(contentUri, "audio/*")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            this.startActivity(intent)
        }

        btnViewImages.setOnClickListener {
            val intentViewPicsActivity = Intent(this, ViewPicturesActivity::class.java)
            intentViewPicsActivity.putExtra(ViewPicturesActivity.SCORE_DIR, score.dir)
            startActivity(intentViewPicsActivity)
//            intent.action = Intent.ACTION_VIEW
//            val uri = FileProvider.getUriForFile(
//                this,
//                "com.evmg.musicscoreapp.fileprovider",
//                File(score.dir))
//            intent.setDataAndType(uri, "*/*")
//                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            startActivity(Intent.createChooser(intent, "Open images"))
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isImport()) {
            menuInflater.inflate(R.menu.import_score_menu, menu)
        } else {
            menuInflater.inflate(R.menu.view_score_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_score) {
            confirmDialog()
        }
        if (item.itemId == R.id.edit_score) {
            val intent = Intent(this, CreateOrUpdateScoreActivity::class.java)
            intent.putExtra(CreateOrUpdateScoreActivity.SCORE_PATH, score.dir)
            startActivity(intent)
            finish()
        }
        if (item.itemId == R.id.import_score) {
            scoreDb.moveScoreToPermanent(score)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Delete ${score.title}?")
        dialogBuilder.setMessage("Are you sure you want to delete ${score.title}?")
        dialogBuilder.setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
            ScoreDb(this).deleteScore(score.dir)
            finish()
        })
        dialogBuilder.setNegativeButton("No", DialogInterface.OnClickListener { _, _ -> })
        dialogBuilder.create().show()
    }

    private fun getScoreFromIntent(): Score? {
        if (intent.hasExtra(SCORE_PATH)) {
            return scoreDb.getScore(intent.getStringExtra(SCORE_PATH)!!)
        }
        return null
    }

    private fun isImport(): Boolean {
        return intent.hasExtra(TYPE) && intent.getStringExtra(TYPE) == TYPE_IMPORT
    }
}
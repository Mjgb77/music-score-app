package com.example.musicscoreapp

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class ScoreDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_details)
        fillIntentData()

        titleInputDetails = findViewById(R.id.title_input_details)

        //Get the score
        val databaseHelper = DatabaseHelper(this)
        score = databaseHelper.getById(id)


        titleInputDetails?.setText(score?.title)
        supportActionBar?.title = score?.title

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
         menuInflater.inflate(R.menu.details_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.delete_score){
            confirmDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmDialog(){
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Delete ${score?.title}?")
        dialogBuilder.setMessage("Are you sure you want to delete ${score?.title}?")
        dialogBuilder.setPositiveButton("Yes", DialogInterface.OnClickListener{ _,_ ->
            DatabaseHelper(this).deleteScore(score?.id as Int)
            finish()
        })
        dialogBuilder.setNegativeButton("No", DialogInterface.OnClickListener{_,_ -> })
        dialogBuilder.create().show()
    }

   private fun fillIntentData(){
        if(intent.hasExtra("id")){
            id = intent.getIntExtra("id",0)
        }else{
            Toast.makeText(this, "No data", Toast.LENGTH_LONG)
            finish()
        }
    }

    companion object{
        private var titleInputDetails : EditText? = null
        private var id: Int = 0
        private var score: MusicScore? = null
    }
}
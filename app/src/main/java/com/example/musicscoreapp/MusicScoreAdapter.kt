package com.example.musicscoreapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicscoreapp.MusicScoreAdapter.MyViewHolder
import java.util.*

class MusicScoreAdapter internal constructor(private val context: Context, private val activity: Activity, private val databaseHelper: DatabaseHelper = DatabaseHelper(context), private var musicScores: ArrayList<MusicScore> = databaseHelper.getAllScores())

    : RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.music_score_row, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val musicScore = musicScores[position]
        holder.txtTitle.text = musicScore.title

        holder.mainLayout.setOnClickListener { _->
            val intent = Intent(context, ScoreDetailsActivity::class.java)
            intent.putExtra("id", musicScore.id)
            activity.startActivityForResult(intent, 1)
        }
    }

    override fun getItemCount(): Int {
        if(musicScores.size != databaseHelper.getScoreCount()){
            musicScores = databaseHelper.getAllScores()
        }

        return musicScores.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val txtTitle: TextView = itemView.findViewById(R.id.txt_title)
        internal val mainLayout: LinearLayout = itemView.findViewById(R.id.mainLayout)


    }
}
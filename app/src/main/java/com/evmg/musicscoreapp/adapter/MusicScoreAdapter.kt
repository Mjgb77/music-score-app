package com.evmg.musicscoreapp.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider.getUriForFile
import androidx.recyclerview.widget.RecyclerView
import com.evmg.musicscoreapp.R
import com.evmg.musicscoreapp.activities.ScoreDetailsActivity
import com.evmg.musicscoreapp.adapter.MusicScoreAdapter.MyViewHolder
import com.evmg.musicscoreapp.model.Score
import com.evmg.musicscoreapp.viewmodels.AddScoreViewModel
import java.io.File
import java.text.DateFormat
import java.util.*

class MusicScoreAdapter internal constructor(
    private val context: Context,
    private val activity: Activity,
    private val musicScores: List<Score>)
    : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_music_score, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val score = musicScores[position]
        holder.textTitle.text = score.title
        holder.textInstrument.text = AddScoreViewModel.INSTRUMENTS_BY_ID[score.instrument] ?: score.instrument.toString()
        holder.textDate.text = DateFormat.getDateInstance().format(Date(score.createDate))


        holder.mainLayout.setOnClickListener { _->
            val intent = Intent(context, ScoreDetailsActivity::class.java)
            intent.putExtra(ScoreDetailsActivity.SCORE_PATH, score.dir)
            activity.startActivityForResult(intent, 1)
        }

        val btnMenu = holder.itemView.findViewById<ImageButton>(R.id.btnActions)
        btnMenu.setOnClickListener { _ ->
            showPopupMenu(btnMenu, position)
        }

        val btnPlay = holder.itemView.findViewById<ImageButton>(R.id.btnPlayScore)
        val scoreFolder = File(score.dir)

        btnPlay.setOnClickListener { _ ->
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW

            //TODO use dynamically generated audio file name
            val contentUri: Uri = getUriForFile(context, "com.evmg.musicscoreapp.fileprovider", File(scoreFolder.path, "audio.mid"))

            intent.setDataAndType(contentUri, "audio/*")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return musicScores.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        internal val textInstrument: TextView = itemView.findViewById(R.id.textInstrument)
        internal val textDate: TextView = itemView.findViewById(R.id.textDate)
        internal val mainLayout: ConstraintLayout = itemView.findViewById(R.id.mainLayout)
    }

    internal class MyMenuItemClickListener(private val position: Int) : PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {

                else -> {
                }
            }
            return false
        }
    }

    private fun showPopupMenu(view: View, position: Int) {
        // inflate menu
        val popup = PopupMenu(view.context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.details_menu, popup.menu)
        popup.setOnMenuItemClickListener(MyMenuItemClickListener(position))
        popup.show()
    }
}
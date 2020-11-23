package com.example.musicscoreapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.FileProvider.getUriForFile
import androidx.recyclerview.widget.RecyclerView
import com.example.musicscoreapp.MusicScoreAdapter.MyViewHolder
import java.io.File
import java.io.FileFilter
import java.util.*


class MusicScoreAdapter internal constructor(private val context: Context, private val activity: Activity)
    : RecyclerView.Adapter<MyViewHolder>() {

    private val fileStorageHelper: FileStorageHelper = FileStorageHelper(context)
    private var musicScores: ArrayList<MusicScore> = fileStorageHelper.getAllScores()

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
            intent.putExtra("scoreTitle", musicScore.title)
            activity.startActivityForResult(intent, 1)
        }

        val btnMenu = holder.itemView.findViewById<ImageButton>(R.id.btn_score_menu)
        btnMenu.setOnClickListener { _ ->
            showPopupMenu(btnMenu, position)
        }

        val btnPlay = holder.itemView.findViewById<ImageButton>(R.id.btn_play_main)
        val scoreFolder = File(musicScore.path)

        btnPlay.setOnClickListener { _ ->
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW

            //TODO use dynamically generated audio file name
            val contentUri: Uri = getUriForFile(context, "com.example.musicscoreapp", File(scoreFolder.path, "audio.mid"))

            intent.setDataAndType(contentUri, "audio/*")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        }

        val imageView = holder.itemView.findViewById<AppCompatImageView>(R.id.img_score_preview)
        //get first image
        val firstImage = scoreFolder.listFiles(FileFilter { f -> f.nameWithoutExtension == "1" }).firstOrNull()

        if(firstImage != null) {
            //TODO crop and set bitmap
         //   imageView.setImageURI(firstImage.path.toUri())
        }


    }

    override fun getItemCount(): Int {
       if(musicScores.size != fileStorageHelper.getScoreCount()){
            musicScores = fileStorageHelper.getAllScores()
        }

        return musicScores.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val txtTitle: TextView = itemView.findViewById(R.id.txt_title)
        internal val mainLayout: LinearLayout = itemView.findViewById(R.id.mainLayout)
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
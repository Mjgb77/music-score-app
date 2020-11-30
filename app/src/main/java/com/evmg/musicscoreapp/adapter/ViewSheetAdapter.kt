package com.evmg.musicscoreapp.adapter

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.evmg.musicscoreapp.viewmodels.AddScoreViewModel
import com.evmg.musicscoreapp.R
import com.evmg.musicscoreapp.objectparsing.SheetMusic
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class ViewSheetAdapter(private val sheets: List<SheetMusic>) :
    RecyclerView.Adapter<ViewSheetAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setImageURI(Uri.fromFile(sheets[position].imageFile))
        holder.fabRemove.hide()
    }

    override fun getItemCount(): Int {
        return sheets.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageSheet)
        val fabRemove: FloatingActionButton = itemView.findViewById(R.id.fabRemoveSheet)
    }

}

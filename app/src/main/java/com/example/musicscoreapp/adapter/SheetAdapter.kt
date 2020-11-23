package com.example.musicscoreapp.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicscoreapp.AddScoreViewModel
import com.example.musicscoreapp.R
import com.example.musicscoreapp.SheetMusic
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.util.*

class SheetAdapter(private val viewModel: AddScoreViewModel) :
    RecyclerView.Adapter<SheetAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.setImageBitmap(viewModel.sheets[position].image)
        holder.fabRemove.setOnClickListener { remove(position) }
    }

    override fun getItemCount(): Int {
        return viewModel.sheets.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageSheet)
        val fabRemove: FloatingActionButton = itemView.findViewById(R.id.fabRemoveSheet)
    }

    fun add(image: Bitmap, imageFile: File) {
        viewModel.addSheet(image, imageFile)
        notifyItemInserted(itemCount)
    }

    private fun remove(position: Int) {
        viewModel.removeSheet(position)
        notifyItemRemoved(position)
        //notifyItemRangeChanged(position, itemCount)
    }

}

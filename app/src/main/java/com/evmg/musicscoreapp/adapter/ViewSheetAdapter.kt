package com.evmg.musicscoreapp.adapter

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.evmg.musicscoreapp.model.StoredScore
import com.evmg.musicscoreapp.R
import com.evmg.musicscoreapp.service.PictureService
import com.evmg.musicscoreapp.service.ScoreToMidiConverter
import com.evmg.musicscoreapp.utils.ImageUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class ViewSheetAdapter(private val context: Activity, private val storedScore: StoredScore) :
    RecyclerView.Adapter<ViewSheetAdapter.ViewHolder>() {
    val augmented = hashSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.gallery_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fabRemove.hide()
        holder.image.setImageURI(Uri.fromFile(storedScore.score.sheets!![position].imageFile))
//        holder.image.setOnTouchListener(ImagePincher())
        holder.image.setOnClickListener {
            showImage(storedScore.score.sheets[position].imageFile, position)
        }
    }

    override fun getItemCount(): Int {
        return storedScore.score.sheets!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imageSheet)
        val fabRemove: FloatingActionButton = itemView.findViewById(R.id.fabRemoveSheet)
    }

    fun showImage(file: File, position: Int) {
        val builder = Dialog(context)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        builder.setOnDismissListener {
            //nothing;
        }
        val imageView = ImageView(context)
        imageView.setImageURI(Uri.fromFile(file))
        imageView.setOnTouchListener(ImagePincher())
        imageView.setOnClickListener {
            GlobalScope.launch {
                if (augmented.contains(position)) {
                    augmented.remove(position)
                    withContext(Dispatchers.Main) {
                        imageView.setImageURI(Uri.fromFile(storedScore.score.sheets!![position].imageFile))
                    }
                } else {
                    augmented.add(position)
                    val bmp =
                        PictureService.readBitmap(storedScore.score.sheets!![position].imageFile)
//        holder.image.setImageURI(Uri.fromFile(storedScore.score.sheets!![position].imageFile))
                    val recognitions = storedScore.recognitions[position]
                    val mutable = bmp.copy(bmp.config, true)
                    for (a in recognitions) {
                        ImageUtils.paintRect(
                            mutable,
                            a.staff.location,
                            Color.DKGRAY,
                            Math.max(Math.max(mutable.height, mutable.width) / 800, 1)
                        )
                        for (b in a.objects) {
                            ImageUtils.paintRect(
                                mutable,
                                b.location,
                                ScoreToMidiConverter.COLORS[b.detectedClass % ScoreToMidiConverter.COLORS.size],
                                Math.max(Math.max(mutable.height, mutable.width) / 800, 1)
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        imageView.setImageBitmap(mutable)
                    }
                }
            }
        }
        builder.addContentView(
            imageView, RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        builder.show()
    }

}

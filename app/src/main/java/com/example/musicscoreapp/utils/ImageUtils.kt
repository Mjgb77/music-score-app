package com.example.musicscoreapp.utils

import android.graphics.*
import android.util.Size
import androidx.core.graphics.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


object ImageUtils {

    fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    suspend fun adjustImageToSize(img: Bitmap, desiredSize: Size): Adjustment {
        return withContext(Dispatchers.IO) {
            var adjustment = Adjustment(img) {
                cloneRectF(it,
                        max(it.left, 0F),
                        max(it.top, 0F),
                        min(it.right, img.width - 1F),
                        min(it.bottom, img.height - 1F))
            }
            val croppedImage = { bmp: Bitmap ->
                if (desiredSize.width * bmp.height > bmp.width * desiredSize.height) {
                    // increase width
                    val newWidth = bmp.height * desiredSize.width / desiredSize.height
                    val diff = (newWidth - bmp.width) / 2.0F
                    adjustment = adjustment.withReverser {
                        cloneRectF(it, left = it.left - diff, right = it.right - diff)
                    }
                    val outputimage = Bitmap.createBitmap(newWidth, bmp.height, Bitmap.Config.ARGB_8888)
                    val can = Canvas(outputimage)
                    can.drawRGB(255, 255, 255)
                    can.drawBitmap(bmp, diff, 0F, null)
                    outputimage
                } else {
                    // increase height
                    val newHeight = bmp.width * desiredSize.height / desiredSize.width
                    val diff = (newHeight - bmp.height) / 2.0F
                    adjustment = adjustment.withReverser {
                        cloneRectF(it, top = it.top - diff, bottom = it.bottom - diff)
                    }
                    val outputimage = Bitmap.createBitmap(bmp.width, newHeight, Bitmap.Config.ARGB_8888)
                    val can = Canvas(outputimage)
                    can.drawRGB(255, 255, 255)
                    can.drawBitmap(bmp, 0F, diff, null)
                    outputimage
                }

            }.invoke(img)
            val resizedImage = { bmp: Bitmap ->
                val verticalFactor = 1.0F * bmp.height / desiredSize.height
                val horizontalFactor = 1.0F * bmp.width / desiredSize.width
                var curr = bmp
                while (desiredSize.width * 2 < curr.width) {
                    curr = Bitmap.createScaledBitmap(curr, curr.width / 2, curr.height / 2, true)
                }
                adjustment = adjustment.withReverser {
                    cloneRectF(
                            it,
                            left = it.left * horizontalFactor,
                            top = it.top * verticalFactor,
                            right = it.right * horizontalFactor,
                            bottom = it.bottom * verticalFactor)
                }
                Bitmap.createScaledBitmap(curr, desiredSize.width, desiredSize.height, true)
            }.invoke(croppedImage)
            adjustment.withBmp(resizedImage)
        }
    }

    fun mapRectF(oldSize: Size, newSize: Size, rect: RectF) {
        rect.top = rect.top * newSize.height / oldSize.height
        rect.bottom = rect.bottom * newSize.height / oldSize.height
        rect.left = rect.left * newSize.width / oldSize.width
        rect.right = rect.right * newSize.width / oldSize.width
    }

    fun crop(img: Bitmap, rect: Rect): Adjustment {
        return Adjustment(Bitmap.createBitmap(
                img,
                rect.left,
                rect.top,
                rect.right - rect.left + 1,
                rect.bottom - rect.top + 1)) {
            RectF(
                    it.left + rect.left,
                    it.top + rect.top,
                    it.right + rect.left,
                    it.bottom + rect.top)
        }
    }

    fun paintRect(img: Bitmap, rect: RectF, color: Int) {
        val location = snap(rect)
        for (x in location.left until location.right) {
            img.set(x, location.top, color)
            img.set(x, location.bottom, color)
        }
        for (y in location.top until location.bottom) {
            img.set(location.left, y, color)
            img.set(location.right, y, color)
        }
    }

    fun snap(rect: RectF) : Rect {
        return Rect(
                ceil(rect.left.toDouble()).toInt(),
                ceil(rect.top.toDouble()).toInt(),
                floor(rect.right.toDouble()).toInt(),
                floor(rect.bottom.toDouble()).toInt())
    }

    class Adjustment(
            val bmp: Bitmap,
            val reverser: (RectF) -> RectF = { it }) {
        fun withBmp(bmp: Bitmap) : Adjustment {
            return Adjustment(bmp, reverser)
        }
        fun withReverser(reverser: (RectF) -> RectF) : Adjustment {
            return Adjustment(bmp) { rect -> this.reverser(reverser(rect)) }
        }
    }

    fun cloneRectF(rect: RectF, left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null): RectF {
        return RectF(left ?: rect.left, top ?: rect.top, right ?: rect.right, bottom ?: rect.bottom)
    }

}
/*

indetity
uncrop
descale

reverser = it - > reverser(uncrop(it))
reverser = it - > reverser(unscaler(it))
it -> indetity(uncrop(descale(it)))

 */

package com.evmg.musicscoreapp.service

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.core.graphics.get
import com.evmg.musicscoreapp.model.Recognition
import com.evmg.musicscoreapp.utils.ImageUtils
import kotlin.math.round

object ObjectAdjustments {

    fun adjustBarline(recognition: Recognition, bitmap: Bitmap): Recognition {
        return adjustBarlineWithParams(
                        recognition,
                        bitmap,
                        BarlineParams(5F, 1F, 0.5F))
    }

    private fun adjustBarlineWithParams(
            recognition: Recognition,
            bitmap: Bitmap,
            params: BarlineParams): Recognition {
        val rect = ImageUtils.snap(recognition.location)
        val delta = recognition.location.height() / params.divider
        val getBlackness = { y: Int ->
            (rect.left..rect.right).sumBy { x ->
                grayScale(bitmap.get(x, y))
            }
        }
        val dUp = (round(-delta * params.moveOut).toInt() .. round(delta * params.moveIn).toInt())
                .map { rect.top + it }
                .filter { it >= 0 }
                .filter { it < bitmap.height }
                .withIndex()
                .minByOrNull { y -> getBlackness(y.value) }!!.value
        val dDown = (round(-delta * params.moveIn).toInt() .. round(delta * params.moveOut).toInt())
                .map { rect.bottom + it }
                .filter { it >= 0 }
                .filter { it < bitmap.height }
                .reversed()
                .withIndex()
                .minByOrNull { y -> getBlackness(y.value) }!!.value

        return Recognition(
                recognition.id,
                recognition.title,
                recognition.confidence,
                RectF(
                        recognition.location.left,
                        dUp.toFloat(),
                        recognition.location.right,
                        dDown.toFloat()),
                recognition.detectedClass)
    }

    private fun grayScale(color: Int): Int {
        return listOf(
                color.shl(16).and(0xFF),
                color.shl(8).and(0xFF),
                color.and(0xFF)).sum()
    }

    class BarlineParams(
            val divider: Float,
            val moveOut: Float,
            val moveIn: Float)


}
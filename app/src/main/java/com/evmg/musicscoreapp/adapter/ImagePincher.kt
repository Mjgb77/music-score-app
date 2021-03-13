package com.evmg.musicscoreapp.adapter

import android.graphics.Matrix
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import java.lang.Math.sqrt

class ImagePincher: View.OnTouchListener {

    // These matrices will be used to move and zoom image
    var matrix: Matrix = Matrix()
    var savedMatrix: Matrix = Matrix()

    // We can be in one of these 3 states
    val NONE = 0
    val DRAG = 1
    val ZOOM = 2
    var mode = NONE

    // Remember some things for zooming
    var start = PointF()
    var mid = PointF()
    var oldDist = 1f
    var lastDown = 0L

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view = v as ImageView
        // make the image scalable as a matrix
        view.scaleType = ImageView.ScaleType.MATRIX
        val scale: Float
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
                lastDown = System.currentTimeMillis()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (mode == DRAG && System.currentTimeMillis() - lastDown < 100) {
                    v.performClick()
                }
                mode = NONE
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event) // calculates the distance between two points where user touched.
                // minimal distance between both the fingers
                if (oldDist > 5f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event) // sets the mid-point of the straight line between two points where user touched.
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) { //movement of first finger
                matrix.set(savedMatrix)
                if (view.left >= -392) {
                    matrix.postTranslate(event.x - start.x, event.y - start.y)
                }
            } else if (mode == ZOOM) { //pinch zooming
                val newDist = spacing(event)
                if (newDist > 5f) {
                    matrix.set(savedMatrix)
                    scale = newDist / oldDist //thinking I need to play around with this value to limit it**
                    matrix.postScale(scale, scale, mid.x, mid.y)
                }
            }
        }

        // Perform the transformation
        view.imageMatrix = matrix
        return true // indicate event was handled
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1).toDouble()
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }
}
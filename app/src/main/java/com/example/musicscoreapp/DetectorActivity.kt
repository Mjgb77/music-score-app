package com.example.musicscoreapp

import android.media.ImageReader.OnImageAvailableListener
import android.util.Size

class DetectorActivity : CameraActivity(), OnImageAvailableListener {

    private val desiredPreviewSize = Size(640, 480)

    override fun onPreviewSizeChosen(size: Size?, rotation: Int) {
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_camera_connection;
    }

    override fun getDesiredPreviewFrameSize(): Size {
      return desiredPreviewSize
    }
}
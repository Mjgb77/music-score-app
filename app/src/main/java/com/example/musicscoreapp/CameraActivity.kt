package com.example.musicscoreapp

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

abstract class CameraActivity : AppCompatActivity(), ImageReader.OnImageAvailableListener{
   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        setFragment()

    }

    protected var previewWidth = 0
    protected var previewHeight = 0

    override fun onImageAvailable(reader: ImageReader?) {
        val image = reader!!.acquireLatestImage() ?: return
        image.close()
    }

    protected fun setFragment() {
        val cameraId = chooseCamera()
        val fragment: Fragment

            val camera2Fragment: CameraConnectionFragment = CameraConnectionFragment.newInstance(
                    object : CameraConnectionFragment.ConnectionCallback {
                        override fun onPreviewSizeChosen(size: Size?, cameraRotation: Int) {
                            previewHeight = size?.height ?: 0
                            previewWidth = size?.width ?: 0
                            this@CameraActivity.onPreviewSizeChosen(size, cameraRotation)
                        }
                    },
                    this,
                    getLayoutId(),
                    getDesiredPreviewFrameSize()
            )
            camera2Fragment.setCamera(cameraId)
            fragment = camera2Fragment

      supportFragmentManager.beginTransaction().replace(R.id.cameraContainer, fragment).commit()
    }

    private fun chooseCamera(): String? {
        val manager =  this.getSystemService(CAMERA_SERVICE) as CameraManager

        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: continue

                return cameraId
            }
        } catch (e: CameraAccessException) {
            throw e
        }
        return null
    }

    protected abstract fun onPreviewSizeChosen(size: Size?, rotation: Int)
    protected abstract fun getLayoutId(): Int
    protected abstract fun getDesiredPreviewFrameSize(): Size
}
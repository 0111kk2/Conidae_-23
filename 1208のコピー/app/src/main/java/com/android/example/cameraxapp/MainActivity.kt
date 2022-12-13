package com.android.example.cameraxapp



import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.getDisplay
import androidx.lifecycle.LifecycleOwner
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {


    private val TAG = "MyApp"
    private val REQUEST_CODE_FOR_PERMISSIONS = 1234
    private val REQUIRED_PERMISSIONS =
        arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE")

    //view
    private var previewView: PreviewView? = null
    private var imageView: ImageView? = null


    //camerax
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.previewView)
        imageView = findViewById(R.id.imageView)

        // Request permissions
        if (checkPermissions()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_FOR_PERMISSIONS);
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val context: Context = this
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                preview = Preview.Builder().build()
                imageAnalysis = ImageAnalysis.Builder().build()
                imageAnalysis!!.setAnalyzer(cameraExecutor, MyImageAnalyzer())
                val cameraSelector =
                    CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle((context as LifecycleOwner), cameraSelector, preview, imageAnalysis)
                preview!!.setSurfaceProvider(previewView!!.getSurfaceProvider());
            } catch (e: Exception) {
                Log.e(TAG, "[startCamera] Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private class MyImageAnalyzer : ImageAnalysis.Analyzer {

        private var matPrevious: Mat? = null

        override fun analyze(image: ImageProxy) {
            //TODO("Not yet implemented")
            val matOrg: Mat = MatFromImage(image)
            val mat = matOrg

            Log.i(TAG, "[analyze] width = " + image.getWidth() + ", height = " + image.getHeight() )
            Log.i(TAG, "[analyze] mat width = " + matOrg.cols() + ", mat height = " + matOrg.rows())



            val matOutput = Mat(mat.rows(), mat.cols(), mat.type())
            if (matPrevious == null) matPrevious = mat
            Core.absdiff(mat, matPrevious, matOutput)
            matPrevious = mat;

            /* Draw something for test */
            Imgproc.rectangle(
                matOutput,
                Rect(10, 10, 100, 100), Scalar(255.0, 0.0, 0.0))
            Imgproc.putText(matOutput, "leftTop", Point(10.0, 10.0), 1, 1.0,
                Scalar(255.0, 0.0, 0.0))

            /* Convert cv::mat to bitmap for drawing */
            val bitmap: Bitmap = Bitmap.createBitmap(matOutput.cols(), matOutput.rows(), Bitmap.Config.ARGB_8888)

            Utils.matToBitmap(matOutput, bitmap)

            /* Display the result onto ImageView */
            //runOnUiThread(Runnable {imageView.setImageBitmap(bitmap) })

            /* Close the image otherwise, this function is not called next time */
            image.close()

        }

        private fun runOnUiThread(runnable: Runnable) {

        }


        private fun MatFromImage(image: ImageProxy): Mat {
            /* https://stackoverflow.com/questions/30510928/convert-android-camera2-api-yuv-420-888-to-rgb */
            val yBuffer = image.planes[0].buffer
            val uBuffer = image.planes[1].buffer
            val vBuffer = image.planes[2].buffer
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer[nv21, 0, ySize]
            vBuffer[nv21, ySize, vSize]
            uBuffer[nv21, ySize + vSize, uSize]
            val yuv = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
            yuv.put(0, 0, nv21)
            val mat = Mat()
            Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGB_NV21, 3)
            return mat
        }
       /* private fun fixMatRotation(matOrg: Mat): Mat {
            var mat: Mat
            when (previewView.getDisplay().getRotation()) {
                Surface.ROTATION_0 -> {
                    mat = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                    Core.transpose(matOrg, mat)
                    Core.flip(mat, mat, 1)
                }
                Surface.ROTATION_90 -> mat = matOrg
                Surface.ROTATION_270 -> {
                    mat = matOrg
                    Core.flip(mat, mat, -1)
                }
                else -> {
                    mat = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                    Core.transpose(matOrg, mat)
                    Core.flip(mat, mat, 1)
                }
            }
            return mat;
        }*/
    }

    private fun checkPermissions(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_FOR_PERMISSIONS) {
            if (checkPermissions()) {
                startCamera()
            } else {
                Log.i(TAG, "[onRequestPermissionsResult] Failed to get permissions")
                finish()
            }
        }
    }
}


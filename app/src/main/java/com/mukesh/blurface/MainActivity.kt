package com.mukesh.blurface

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions


class MainActivity : AppCompatActivity() {

    private val cameraRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK){
            val photo = it.data?.extras?.get("data") as Bitmap
            detectFaces(photo)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        findViewById<Button>(R.id.btPickImage).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 200);
            }else {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraRequest.launch(cameraIntent)
            }
        }
    }


    private fun detectFaces(bitmap: Bitmap) {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()

        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        detector.detectInImage(FirebaseVisionImage.fromBitmap(bitmap))
            .addOnSuccessListener { faces ->
                // Create a blurred image
                blurFaces(bitmap, faces)
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error detecting faces: $e")
            }
    }


    private fun blurFaces(bitmap: Bitmap, faces: List<FirebaseVisionFace>) {
        // Create a mutable copy of the original image
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Create a canvas for drawing on the image
        val canvas = Canvas(mutableBitmap)

        // Create a paint for blurring the faces
        val paint = Paint()
        paint.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)

        // Draw rectangles over each face in the image and blur them
        for (face in faces) {
            val bounds = face.boundingBox
            paint.color = Color.parseColor("#ecbcb4")
            canvas.drawRect(Rect(bounds), paint)
        }

        // Update the image view with the blurred image
        findViewById<ImageView>(R.id.ivImage).setImageBitmap(mutableBitmap)
    }

}
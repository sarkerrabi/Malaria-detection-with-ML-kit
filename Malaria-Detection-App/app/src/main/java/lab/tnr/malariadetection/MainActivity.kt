package lab.tnr.malariaditection

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import lab.tnr.malariadetection.tflite.Classifier



class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val mInputSize = 32
    private val mModelPath = "converted_autoencoder_final_result_32.tflite"
    private val mLabelPath = "myLable.txt"
    private lateinit var classifier: Classifier

    private var imgView: ImageView? = null
    private var resultView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_updated)
        initViews()
        initClassifier()
    }

    private fun initViews() {
        findViewById<Button>(R.id.btChooseImg).setOnClickListener(this)
        findViewById<Button>(R.id.btCaptureImg).setOnClickListener(this)
        imgView  = findViewById<ImageView>(R.id.ivChooseImage)
        resultView = findViewById<TextView>(R.id.tvResultMessage)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btChooseImg) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE)
                } else {
                    //permission already granted
                    pickImageFromGallery()
                }
            } else {
                //system OS is < Marshmallow
                pickImageFromGallery()
            }
        } else if (v?.id == R.id.btCaptureImg) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.CAMERA)
                    //show popup to request runtime permission
                    requestPermissions(permissions, CAMERA_PERMISSION_CODE)
                } else {
                    //permission already granted
                    dispatchTakePictureIntent()
                }
            } else {
                //system OS is < Marshmallow
                dispatchTakePictureIntent()
            }
        }




    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    dispatchTakePictureIntent()
                } else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imgView?.setImageURI(data?.data)
            //val bitmap = ((view as ImageView).drawable as BitmapDrawable).bitmap
            val bitmap =
                MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)

            val result = classifier.recognizeImage(bitmap)

            resultView?.text = "" + result[0].title

            runOnUiThread { Toast.makeText(this, result[0].title, Toast.LENGTH_SHORT).show() }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgView?.setImageBitmap(imageBitmap)

            val result = classifier.recognizeImage(imageBitmap)

            resultView?.text = "" + result[0].title

            runOnUiThread { Toast.makeText(this, result[0].title, Toast.LENGTH_SHORT).show() }


        }
    }

    private fun initClassifier() {
        classifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)
    }


    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000

        //capture image
        private val REQUEST_IMAGE_CAPTURE = 1

        //Permission code
        private val PERMISSION_CODE = 1001

        //Permission code
        private val CAMERA_PERMISSION_CODE = 1002


    }


}

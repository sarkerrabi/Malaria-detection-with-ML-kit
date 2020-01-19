package lab.tnr.malariaditection

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import lab.tnr.malariaditection.tflite.Classifier


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val mInputSize = 32
    private val mModelPath = "converted_autoencoder_final_result_32.tflite"
    private val mLabelPath = "myLable.txt"
    private lateinit var classifier: Classifier

    private var imgView: ImageView? = null
    private var resultView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initClassifier()
    }

    private fun initViews() {
        findViewById<TextView>(R.id.tvChooseImg).setOnClickListener(this)
        imgView  = findViewById<ImageView>(R.id.ivChooseImage)
        resultView = findViewById<TextView>(R.id.tvResultMessage)
    }

    override fun onClick(v: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE)
            }
            else{
                //permission already granted
                pickImageFromGallery()
            }
        }
        else{
            //system OS is < Marshmallow
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
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

            resultView?.setText(""+result[0].title)

            runOnUiThread { Toast.makeText(this, result[0].title, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun initClassifier() {
        classifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)
    }


    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
    }


}

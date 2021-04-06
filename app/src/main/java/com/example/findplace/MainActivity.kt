package com.example.findplace

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private val GET_GALLERY_IMAGE : Int = 200
    private val photoInfoMap : HashMap<String, String> = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image.setOnClickListener {
            val intent : Intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, GET_GALLERY_IMAGE)
        }

        textMain.setOnClickListener {
            if((it as TextView).text.equals("아래를 클릭해서 사진을 등록하세요"))
                return@setOnClickListener

            val dlgView = View.inflate(this@MainActivity, R.layout.detail_info_layout, null)
            val dlg = AlertDialog.Builder(this@MainActivity).create()

            val textDate : TextView = findViewById(R.id.photoDate)
            val textLabel : TextView = findViewById(R.id.photoLabel)
            val textLocation : TextView = findViewById(R.id.photoLocation)
            val textDevice : TextView = findViewById(R.id.photoDevice)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GET_GALLERY_IMAGE && resultCode == Activity.RESULT_OK) {
            data.let {
                data?.data.let {
                    try {
                        val selectedImageUri = data?.data!!
                        image.setImageURI(selectedImageUri)
                        val exif = ExifInterface(getAbsolutePath(selectedImageUri))
                        val degree = GeoDegree(exif)
                        textMain.text = exif.getAttribute(ExifInterface.TAG_MAKE) + "/" + exif.getAttribute(ExifInterface.TAG_MODEL) + "\r\n" +
                                getAddress(degree.getLatitude().toDouble(), degree.getLongitude().toDouble())

                        Toast.makeText(this@MainActivity, "Test Address : ${getAddress(degree.getLatitude().toDouble(), degree.getLongitude().toDouble())}", Toast.LENGTH_SHORT).show()
                    } catch (e : Exception) {
                        Toast.makeText(this@MainActivity, "Result Error", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    //위도와 경도를 주소(String)으로 변환
    private fun getAddress(latitude : Double, longitude : Double) : String {
        //Geocoder 인스턴스 생성
        val geo : Geocoder = Geocoder(this@MainActivity)

        //위도와 경도를 매개변수로 Address 객체 반환
        val addressList : List<Address> = geo.getFromLocation(latitude, longitude, 10)

        val buffer = StringBuffer(addressList.get(0).countryName)

        buffer.append(" ${addressList.get(0).locality}")
        //buffer.append(" ${addressList.get(0).subLocality}")
        buffer.append(" ${addressList.get(0).thoroughfare}")
        //buffer.append(" ${addressList.get(0).featureName}")

        return buffer.toString()
    }

    //Uri를 절대경로(Absolute Path)로 변환
    private fun getAbsolutePath(uri : Uri) : String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val c : Cursor = contentResolver.query(uri, proj, null, null)!!
        val index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c.moveToFirst()

        val result = c.getString(index)

        return result
    }
}
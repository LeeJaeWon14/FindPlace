package com.example.findplace

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kakao.util.maps.helper.Utility.getPackageInfo
import kotlinx.android.synthetic.main.activity_main.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {
    private val GET_GALLERY_IMAGE : Int = 200
    private lateinit var photoInfoMap : HashMap<String, String>
    private val geoInfoMap : HashMap<String, Double> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        //HashKey 가져옴
        getHashKey()

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

            dlg.setView(dlgView)
            dlg.window.setBackgroundDrawableResource(R.drawable.block)

            val textDate : TextView = dlgView.findViewById(R.id.photoDate)
            val textResolution : TextView = dlgView.findViewById(R.id.photoResolution)
            val textLocation : TextView = dlgView.findViewById(R.id.photoLocation)
            val textDevice : TextView = dlgView.findViewById(R.id.photoDevice)

            textDate.text = "촬영시간\r\n${photoInfoMap.get("date")}"
            textResolution.text = "해상도\r\n${photoInfoMap.get("resolution")}"
            textLocation.text = "위치\r\n${photoInfoMap.get("location")}"
            textDevice.text = "촬영기기\r\n${photoInfoMap.get("device")}"

            dlg.show()
        }

        viewLocationButton.setOnClickListener {
            if(textMain.text.equals("아래를 클릭해서 사진을 등록하세요")) {
                Toast.makeText(this@MainActivity, "사진을 먼저 등록해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this@MainActivity, "실제 위치와 오차가 있을 수 있습니다.", Toast.LENGTH_SHORT).show()

            val dlgView = View.inflate(this@MainActivity, R.layout.map_layout, null)
            val dlg = AlertDialog.Builder(this@MainActivity).create()

            dlg.setView(dlgView)

            //카카오맵 초기화
            val mapView = MapView(this@MainActivity)
            val mapViewContainer = dlgView.findViewById<ViewGroup>(R.id.mapViewL)
            val mapPoint = MapPoint.mapPointWithGeoCoord(geoInfoMap.get("latitude")!!, geoInfoMap.get("longitude")!!)

            //마커 설정
            val marker : MapPOIItem = MapPOIItem()
            marker.itemName = "위치"
            marker.tag = 0
            marker.mapPoint = mapPoint
            marker.markerType = MapPOIItem.MarkerType.BluePin
            marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
            mapView.addPOIItem(marker)

            mapViewContainer.addView(mapView)
            mapView.isHDMapTileEnabled = true //카카오맵의 지도 타일을 고화질 타일로 변경
            mapView.setMapCenterPoint(mapPoint, true) //실행 애니메이션 설정

            //위도와 경조로 중심점 설정
            mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(geoInfoMap.get("latitude")!!, geoInfoMap.get("longitude")!!), true)

            dlg.show()
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

                        //EXIF에 위치정보가 존재하지 않는 경우
                        if(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) == null) {
                            viewLocationButton.visibility = View.INVISIBLE
                            Toast.makeText(this@MainActivity, "위치 정보가 존재하지 않는 사진입니다.", Toast.LENGTH_SHORT).show()
                        }
                        //위치정보가 존재할 경우
                        else {
                            viewLocationButton.visibility = View.VISIBLE
                            val degree = GeoDegree(exif)
                            textMain.text = "상세정보"

                            val address = getAddress(degree.getLatitude().toDouble(), degree.getLongitude().toDouble())
                            geoInfoMap.put("latitude", degree.getLatitude().toDouble())
                            geoInfoMap.put("longitude", degree.getLongitude().toDouble())
                            photoInfoMap = saveExifData(exif, address)
                        }

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

        return addressList.get(0).getAddressLine(0).toString()
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

    //Exif 정보를 HashMap에 담는 메소드
    private fun saveExifData(exif : ExifInterface, location : String) : HashMap<String, String> {
        val map = HashMap<String, String>()

        map.put("date", exif.getAttribute(ExifInterface.TAG_DATETIME)) //날짜
        map.put("resolution", "${exif.getAttribute(ExifInterface.TAG_PIXEL_X_DIMENSION)}x${exif.getAttribute(ExifInterface.TAG_PIXEL_Y_DIMENSION)}") //해상도
        map.put("location", location) //위치
        map.put("device", "${exif.getAttribute(ExifInterface.TAG_MAKE)}, ${exif.getAttribute(ExifInterface.TAG_MODEL)}") //제조사와 모델명

        return map
    }

    //HashKey 가져오는 메소드
    private fun getHashKey() {
        var packageInfo:PackageInfo? = null
        try
        {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES)
        }
        catch (e:PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null")
        for (signature in packageInfo!!.signatures)
        {
            try
            {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
            catch (e: NoSuchAlgorithmException) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e)
            }
        }
    }

    //permission check
    //출처 : https://github.com/ParkSangGwon/TedPermission
    fun checkPermission() {
        val permissionListener : PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() { //권한 있음
                Toast.makeText(this@MainActivity, "권한 허용", Toast.LENGTH_SHORT).show()
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { //권한 없음
                Toast.makeText(this@MainActivity, "권한 거부", Toast.LENGTH_SHORT).show()
            }
        }
        TedPermission.with(this)
            .setPermissionListener(permissionListener) //Listener set
            .setDeniedMessage("권한을 허용하지 않으면 앱이 정상적으로 작동하지 않을 수 있습니다.") //DeniedMessage (Do not granted)
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION) //Granted
            .check()
    }

    //뒤로가기 두번 클릭하면 종료
    private var time : Long = 0
    override fun onBackPressed() {
        if(System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis()
            Toast.makeText(this@MainActivity, "한번 더 누르면 종료합니다", Toast.LENGTH_SHORT).show()
        }
        else if(System.currentTimeMillis() - time < 2000) {
            this.finishAffinity()
        }
    }
}
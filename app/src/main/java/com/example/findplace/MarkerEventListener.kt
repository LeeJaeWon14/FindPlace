package com.example.findplace

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

// 마커 클릭 이벤트 리스너
class MarkerEventListener(val context: Context) : MapView.POIItemEventListener {
    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
        // 말풍선 클릭 시(Deprecated)
        TODO("Not yet implemented")
    }

    override fun onCalloutBalloonOfPOIItemTouched(
        mapView: MapView?,
        mapPOIItem: MapPOIItem?,
        calloutBalloonButtonType: MapPOIItem.CalloutBalloonButtonType?
    ) {
        Log.v("Balloon", "Click")

        // 말풍선 클릭 시
        AlertDialog.Builder(context)
            .setMessage("카카오맵으로 이동하시겠습니까?")
            .setPositiveButton("확인", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    val urlScheme = "kakaomap://open?page=placeSearch"
                    val intent = Intent()
                    intent.apply {
                        setAction(Intent.ACTION_VIEW)
                        addCategory(Intent.CATEGORY_BROWSABLE)
                        addCategory(Intent.CATEGORY_DEFAULT)
                        setData(Uri.parse(urlScheme))
                    }
                    context.startActivity(intent)
                }
            })
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
        // 마커의 isDraggable 속성이 true일 때 마커를 이동시켰을 경우우
        TODO("Not yet implemented")
    }

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        // 마커 클릭 시
        Toast.makeText(context, "Touch", Toast.LENGTH_SHORT).show()
    }
}
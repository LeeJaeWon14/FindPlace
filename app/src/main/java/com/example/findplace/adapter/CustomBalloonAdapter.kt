package com.example.findplace.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.findplace.R
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem

// 커스텀 말풍선 어댑터
class CustomBalloonAdapter(inflater: LayoutInflater, val context: Context) : CalloutBalloonAdapter {
    val balloonView = inflater.inflate(R.layout.balloon_layout, null)
    val address = balloonView.findViewById<TextView>(R.id.balloon_address)

    override fun getPressedCalloutBalloon(mapPOIItem: MapPOIItem?): View {
        address.text = mapPOIItem!!.itemName.toString()
        // 말풍선 클릭 시
        Handler(Looper.getMainLooper()).post(Runnable {
            Toast.makeText(context, "Pressed", Toast.LENGTH_SHORT).show()
        })

        return balloonView
    }

    override fun getCalloutBalloon(mapPOIItem: MapPOIItem?): View {
        address.text = mapPOIItem!!.itemName.toString()
        Handler(Looper.getMainLooper()).post(Runnable {
            AlertDialog.Builder(context)
                .setMessage("카카오맵으로 이동하시겠습니까?")
                .setPositiveButton("확인", object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        val urlScheme = "kakaomap://search?q=${mapPOIItem.itemName}"
                        //"kakaomap://search?q=맛집&p=${mapPOIItem.mapPoint.mapPointGeoCoord.latitude},${mapPOIItem.mapPoint.mapPointGeoCoord.longitude}"
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
        })

        return balloonView
    }

}
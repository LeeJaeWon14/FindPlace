package com.example.findplace.util

import android.media.ExifInterface

class GeoDegree(exif: ExifInterface) {
    private var valid : Boolean = false
    private var latitude : Float = 0f
    private var longitude : Float = 0f

    init {
        MyLogger.i("GeoDegree init!")
        val attrLATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
        val attrLATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
        val attrLONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
        val attrLONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

        MyLogger.i("$attrLATITUDE / $attrLATITUDE_REF}/ $attrLONGITUDE / $attrLONGITUDE_REF")
        if(attrLATITUDE != null && attrLATITUDE_REF != null && attrLONGITUDE != null && attrLONGITUDE_REF != null) {
            valid = true

            if(attrLATITUDE_REF.equals("N")) {
                latitude = convertToDegree(attrLATITUDE)
            }
            else {
                latitude = 0 - convertToDegree(attrLATITUDE)
            }

            if(attrLONGITUDE_REF.equals("E")) {
                longitude = convertToDegree(attrLONGITUDE)
            }
            else {
                longitude = 0 - convertToDegree(attrLONGITUDE)
            }
        }
    }

    private fun convertToDegree(stringDMS : String) : Float {
        var result : Float? = null
        val DMS : List<String> = stringDMS.split(",", limit = 3)

        val stringD : List<String> = DMS.get(0).split("/", limit = 2)
        val D0 : Double = stringD.get(0).toDouble()
        val D1 : Double = stringD.get(1).toDouble()
        val FloatD = D0/D1;

        val stringM = DMS.get(1).split("/", limit = 2)
        val M0 = stringM.get(0).toDouble()
        val M1 = stringM.get(1).toDouble()
        val FloatM = M0/M1

        val stringS = DMS.get(2).split("/", limit = 2)
        val S0 = stringS.get(0).toDouble()
        val S1 = stringS.get(1).toDouble()
        val FloatS = S0/S1

        result = (FloatD + (FloatM/60) + (FloatS/3600)).toFloat()

        return result
    }

    fun isValid() : Boolean {
        return valid
    }

    fun getLatitude() : Float {
        MyLogger.i("latitude >> $latitude")
        return latitude
    }

    fun getLongitude() : Float {
        MyLogger.i("longitude >> $longitude")
        return longitude
    }
}
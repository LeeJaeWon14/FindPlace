package com.example.findplace.util

import android.util.Log

class MyLogger {
    companion object {
        const val TAG = "Custom Log"

        //debug
        fun d(message: String) {
            Log.d(TAG, buildLogMsg(message))
        }

        //Verbose
        fun v(message: String) {
            Log.v(TAG, buildLogMsg(message))
        }

        //info
        fun i(message: String) {
            Log.i(TAG, buildLogMsg(message))
        }

        //Warning
        fun w(message: String) {
            Log.w(TAG, buildLogMsg(message))
        }

        //Error
        fun e(message: String) {
            Log.e(TAG, buildLogMsg(message))
        }

        fun buildLogMsg(message: String): String {
            val ste = Thread.currentThread().stackTrace[4]
            val sb = StringBuilder()
            sb.append("[")
            sb.append(ste.fileName.replace(".java", "", false))
            sb.append("::")
            sb.append(ste.methodName)
            sb.append("]")
            sb.append(message)
            return sb.toString()
        }
    }
}
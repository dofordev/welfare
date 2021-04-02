package com.skt.welfare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService

class Bridge(private val mContext: Context){


    @JavascriptInterface
    fun closeApp() {
        (mContext as MainActivity).finishAffinity()
    }


    @JavascriptInterface
    fun toastPopup(msg: String) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun clipboardCopy(msg: String) {
        val clipboardManager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(mContext.resources.getString(R.string.app_name), msg)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(mContext, Constants.clipToastText, Toast.LENGTH_SHORT).show()
    }
}
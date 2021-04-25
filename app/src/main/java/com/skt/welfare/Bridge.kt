package com.skt.welfare

import DeviceNumberUtil
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*

private const val TAG = "Bridge"
class Bridge(private val mContext: Context){

    private val deviceNumberUtil by lazy { DeviceNumberUtil(mContext) }

    private val webview : WebView = (mContext as MainActivity).mWebView
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

    @JavascriptInterface
    fun getPhoneNumber(callbackFnName: String) {
        if(deviceNumberUtil.enabledUSIM()){
            var phoneNumber = ""
            Log.d(TAG, "toNationalPhoneNumber" + deviceNumberUtil.getSubInfoList()?.size)
            val phoneNumbers = deviceNumberUtil.getPhoneNumberList(deviceNumberUtil.getSubInfoList()).map {
                phoneNumber = it.toNationalPhoneNumber()
            }
            if(phoneNumbers.isNotEmpty()) {
                webview.post(Runnable { webview.loadUrl("javascript:callbackFnName('${phoneNumber}');") })
            } else {
                webview.post(Runnable { webview.loadUrl("javascript:callbackFnName('');") })
            }
        }
        else{
            webview.post(Runnable { webview.loadUrl("javascript:callbackFnName('');") })
        }

    }
    fun String.toNationalPhoneNumber(): String {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val locale = Locale.getDefault().country
        val toNationalNum = phoneNumberUtil.parse(this, locale)
        return phoneNumberUtil.format(
            toNationalNum,
            PhoneNumberUtil.PhoneNumberFormat.NATIONAL
        )
    }
}
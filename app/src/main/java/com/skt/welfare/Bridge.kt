package com.skt.welfare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.Toast
import com.ezdocu.sdk.camera.ExecuteResult
import com.ezdocu.sdk.camera.EzdocuSDK
import com.ezdocu.sdk.camera.TakePictureCallback
import com.ezdocu.sdk.camera.TakePictureOption
import com.google.gson.Gson
import com.skt.Tmap.TMapTapi
import com.skt.welfare.api.BackendApi
import com.skt.welfare.api.OcrResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess


private const val TAG = "Bridge"

class Bridge(private val mContext: Context){


    private val webview : WebView = (mContext as MainActivity).webView
    @JavascriptInterface
    fun closeApp() {
        (mContext as MainActivity).finishAffinity()
        exitProcess(0)
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
        webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}('${Constants.phoneNumber}');") })
    }

    @JavascriptInterface
    fun getLoginInfo(callbackFnName: String) {
        webview.post(Runnable { webview.evaluateJavascript("javascript:${callbackFnName}(${Gson().toJson(Constants.loginInfo)});", ValueCallback(){}) })
    }


    @JavascriptInterface
    fun callOcrCamera(callbackFnName: String, token: String, apiPath : String) {
        Constants.token = token
        EzdocuSDK.open((mContext as MainActivity), Constants.cameraCompanyCode, Constants.cameraJobNo)
        val option = TakePictureOption.CreateInsure43()
        option.retry = 1
        EzdocuSDK.takePicture(option, OcrCallback(callbackFnName, mContext, apiPath))
    }

    @JavascriptInterface
    fun callOcrImage(callbackFnName: String, token: String, apiPath : String){
        Constants.token = token
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra("callbackFnName",callbackFnName)
        intent.putExtra("apiPath",apiPath)
        (mContext as MainActivity).startActivityForResult(Intent.createChooser(intent, ""), IMAGE_PICK_CODE)
    }

    @JavascriptInterface
    fun imageViewer(token: String, fileName: String, filePath: String) {
        Constants.token = token

        val intent = Intent((mContext as MainActivity), ImageActivity::class.java)
        intent.putExtra("fileName", fileName)
        intent.putExtra("filePath", filePath)

        (mContext as MainActivity).startActivityForResult(intent, IMAGE_REQ_CODE)


    }
    @JavascriptInterface
    fun benepia() {

        try{
            val intent: Intent? = (mContext as MainActivity).packageManager.getLaunchIntentForPackage(Constants.benepiaAppId)
            (mContext as MainActivity).startActivity(intent)
        }
        catch(e: java.lang.Exception){
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=" + Constants.benepiaAppId)
            (mContext as MainActivity).startActivity(intent)
        }
    }
    @JavascriptInterface
    fun dywt() {
        if(!checkInstallationOf(mContext, Constants.vacationPackageName)) {
            val installUrl = if(isTablet(mContext)) "toktok://com.sk.tablet.group.store.detail?appId=${Constants.vacationAppId}"
            else "toktok://com.skt.pe.activity.mobileclient.detail?appId=${Constants.vacationAppId}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(installUrl))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_NO_HISTORY
            (mContext as MainActivity).startActivity(intent)
        }
        else{
            val schemeUrl = "dywt://com.gmp.skt.dywt.mobile"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(schemeUrl))
            (mContext as MainActivity).startActivity(intent)
        }
    }

    @JavascriptInterface
    fun tmap(keyword : String, lat : String, long : String) {

        if(lat.equals("") || long.equals("")){
            Toast.makeText(mContext, "지도가 노출된 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
        }
        else{


            val isTmapApp: Boolean? = Constants.tmaptapi?.isTmapApplicationInstalled
            if(isTmapApp != null && isTmapApp){
                Constants.tmaptapi?.invokeRoute(keyword, lat.toFloat(), long.toFloat())
            }
            else{
                var downloadUrl = ""
                val result = Constants.tmaptapi?.tMapDownUrl
                result?.forEach { item -> downloadUrl = item.toString() }
                if(!downloadUrl.equals("")){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                    intent.setPackage("com.android.chrome")
                    (mContext as MainActivity).startActivity(intent)
                }

            }
        }



    }


    @JavascriptInterface
    fun externalBrowser(url: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        (mContext as MainActivity).startActivity(intent)
    }


}

class OcrCallback(callbackFnName : String, context : Context, apiPath : String) : TakePictureCallback {
    val callbackFnName = callbackFnName
    val context = context
    val apiPath = apiPath
    override fun onComplete(executeResult: ExecuteResult?) {
        val lensPosResult = executeResult?.lensPosResult
        val polyImgResult = executeResult?.polyImgResult
        executeResult?.deskew?.data

        executeResult?.deskew?.bitmapBytes()
        val bitmap  = executeResult?.deskew?.toBitmap()//executeResult?.original?.toBitmap()

        Handler().postDelayed({
            sendImage(callbackFnName, context, apiPath, bitmap!!)
        }, 200)



    }

    override fun onCancel() {
        Log.d(TAG, "onCancel")
    }


}
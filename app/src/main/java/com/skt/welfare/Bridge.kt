package com.skt.welfare

import DeviceNumberUtil
import android.R.attr.path
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.ezdocu.sdk.camera.*
import com.google.gson.Gson
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.skt.welfare.api.OcrApi
import com.skt.welfare.api.OcrResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "Bridge"

class Bridge(private val mContext: Context){

    private val deviceNumberUtil by lazy { DeviceNumberUtil(mContext) }

    private val webview : WebView = (mContext as MainActivity).webView
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
                webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}('${phoneNumber}');") })
            } else {
                webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}('');") })
            }
        }
        else{
            webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}('');") })
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

    @JavascriptInterface
    fun callOcrCamera(callbackFnName: String) {
        EzdocuSDK.open((mContext as MainActivity), Constants.cameraCompanyCode, Constants.cameraJobNo)
        val option = TakePictureOption.CreateInsure43()
        option.retry = 1
        EzdocuSDK.takePicture(option, OcrCallback(callbackFnName, webview))
    }


}

class OcrCallback(callbackFnName : String, webview : WebView) : TakePictureCallback {
    val webview = webview
    val callbackFnName = callbackFnName
    override fun onComplete(executeResult: ExecuteResult?) {
        val lensPosResult = executeResult?.lensPosResult
        val polyImgResult = executeResult?.polyImgResult
        executeResult?.deskew?.data

        executeResult?.deskew?.bitmapBytes()
        val bitmap  = executeResult?.original?.toBitmap()


        try {
            val filePath = Environment.getExternalStorageDirectory().absolutePath + "/" + Constants.ocrImgFolder

            val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            val fileName = "ocr_${timeStamp}.png"
            val file: File = File(filePath, fileName)
            val fOut = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()


            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("image/*"), file)
            val body = MultipartBody.Part.createFormData("mstFile", file.getName(), requestFile)
            val api = OcrApi.create()
            api.postOcrImage(body).enqueue(object : Callback<OcrResponse> {
                override fun onResponse(
                    call: Call<OcrResponse>,
                    response: Response<OcrResponse>
                ) {
                    Log.e(TAG, response?.body().toString())
                    // 성공
                    webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}(`${Gson().toJson(response?.body())}`);") })
                }

                override fun onFailure(call: Call<OcrResponse>, t: Throwable) {
                    Log.e(TAG, t.stackTraceToString())
                    // 실패
                    webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}('');") })
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
            webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}('');") })
        }

    }

    override fun onCancel() {
        Log.d(TAG, "onCancel")
    }


}
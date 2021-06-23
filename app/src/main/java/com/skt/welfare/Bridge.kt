package com.skt.welfare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import com.skt.welfare.api.BackendApi
import com.skt.welfare.api.ImageRequest
import com.skt.welfare.api.ImageResponse
import com.skt.welfare.api.OcrResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
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
    fun callOcrCamera(callbackFnName: String, token: String) {
        Constants.token = token
        EzdocuSDK.open((mContext as MainActivity), Constants.cameraCompanyCode, Constants.cameraJobNo)
        val option = TakePictureOption.CreateInsure43()
        option.retry = 1
        EzdocuSDK.takePicture(option, OcrCallback(callbackFnName, mContext))
    }
    @JavascriptInterface
    fun imageViewer(token: String, fileName: String, filePath: String) {
        Constants.token = token

        val intent = Intent((mContext as MainActivity), ImageActivity::class.java)
        intent.putExtra("fileName", fileName)
        intent.putExtra("filePath", filePath)

        (mContext as MainActivity).startActivityForResult(intent, IMAGE_REQ_CODE)


    }

}

class OcrCallback(callbackFnName : String, context : Context) : TakePictureCallback {
    val webview = (context as MainActivity).webView
    val callbackFnName = callbackFnName
    val context = context
    override fun onComplete(executeResult: ExecuteResult?) {
        val lensPosResult = executeResult?.lensPosResult
        val polyImgResult = executeResult?.polyImgResult
        executeResult?.deskew?.data

        executeResult?.deskew?.bitmapBytes()
        val bitmap  = executeResult?.original?.toBitmap()


        try {

            val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            val fileName = "ocr_${timeStamp}.jpeg"


            val out = ByteArrayOutputStream()

            bitmap?.compress(Bitmap.CompressFormat.JPEG, 50 , out)
            out.flush()
            out.close()

            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("image/*"), out.toByteArray())
            val body = MultipartBody.Part.createFormData("mstFile", fileName, requestFile)


            val api = BackendApi.create()
            api.postOcrImage(body).enqueue(object : Callback<OcrResponse> {
                override fun onResponse(
                    call: Call<OcrResponse>,
                    response: Response<OcrResponse>
                ) {

                    // 성공
                    webview.post(Runnable { webview.evaluateJavascript("${callbackFnName}(${Gson().toJson(response?.body())});", ValueCallback(){}) })
                }

                override fun onFailure(call: Call<OcrResponse>, t: Throwable) {
                    Log.e(TAG, t.stackTraceToString())

                    val res = OcrResponse(
                        resultMsg ="응답 없음",
                        resultCd = 888,
                        resultMsgTyp = "E",
                        data = null
                    )

                    // 실패
                    webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}(${Gson().toJson(res)});") })
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
            val res = OcrResponse(
                resultMsg ="알수 없는 에러",
                resultCd = 999,
                resultMsgTyp = "E",
                data = null
            )

            // 실패
            webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}(${Gson().toJson(res)});") })
        }

    }

    override fun onCancel() {
        Log.d(TAG, "onCancel")
    }


}
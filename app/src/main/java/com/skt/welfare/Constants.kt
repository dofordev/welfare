package com.skt.welfare

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.webkit.ValueCallback
import com.google.gson.Gson
import com.skt.Tmap.TMapTapi
import com.skt.welfare.api.BackendApi
import com.skt.welfare.api.OcrResponse
import com.skt.welfare.api.TokTokResponse
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

object Constants {
//    val baseUrl = "https://appsvcmobilefront.z32.web.core.windows.net/"
    val baseUrl = "http://welfaredevm.sktelecom.com:50000"

//    val backendUrl = "https://appsvc-ehr-was.azurewebsites.net"
    val backendUrl = "http://welfaredevm.sktelecom.com:50001"

    val appCloseWaitText = "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다."
    val appCloseText = "앱이 종료됩니다."
    val javascriptBridgeName = "welfare"
    val clipToastText = "클립보드에 복사했습니다"
    val cameraPermissionText = "카메라 권한을 승인해야지만 앱을 사용할 수 있습니다."
    val storagePermissionText = "저장소 권한을 승인해야지만 앱을 사용할 수 있습니다."
    val phonePermissionText = "전화 권한을 승인해야지만 앱을 사용할 수 있습니다."

    val cameraCompanyCode = 1001

    val cameraJobNo: Int = 0

    val ocrImgFolder = "welfare"


    val benepiaAppId = "com.sk.benepia"
    val tmapApiKey = "l7xx6b03efc9ecf74b5488741abe9ffc7392"

    val toktokAppId = "Z000ST0057"
    val toktokDevUrl = "https://devgmp.sktelecom.com:9443"
//    val toktokDevUrl = "https://m.toktok.sk.com:9443"
//    val toktokPrdUrl = "https://devgmp.sktelecom.com:9443"
    val toktokPrdUrl = "https://m.toktok.sk.com:9443"

    val toktokStorePhonePackageName = "com.skt.pe.activity.mobileclient"
    val toktokPhonePackageName = "com.skt.pe.provider"
    val toktokStoreTabletPackageName = "com.sk.tablet.group.store"
    val toktokTabletPackageName = "com.skt.tablet.group.login"

    val toktokPhoneActionName = "com.sk.pe.group.auth.GMP_LOGIN"
    val toktokTabletActionName = "com.sk.pe.auth.GMP_LOGIN"

    val vacationPackageName = "com.gmp.skt.dywt.mobile"
    val vacationAppId = "Z000ST0049";

    val storeUrl = "https://m.toktok.sk.com/fordev.jsp"

    var phoneNumber = ""

    var token = ""

    var loginInfo: TokTokResponse? = null

    var tmaptapi: TMapTapi? = null



    fun sendImage(callbackFnName : String, context : Context, apiPath : String, bitmap : Bitmap){
        val webview = (context as MainActivity).webView
        val callbackFnName = callbackFnName
        val context = context
        val apiPath = apiPath
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


//            val progress = ProgressDialog(context, R.style.MyTheme)
//            progress.setCancelable(false) // disable dismiss by tapping outside of the dialog
//            progress.setProgressStyle(android.R.style.Widget_ProgressBar_Small)
//            progress.show()

            val progress = LoadingDialog(context)
            progress.show()


            val api = BackendApi.create()
            api.postOcrImage(apiPath, body).enqueue(object : Callback<OcrResponse> {
                override fun onResponse(
                    call: Call<OcrResponse>,
                    response: Response<OcrResponse>
                ) {

                    progress.dismiss()
                    // 성공
                    webview.post(Runnable { webview.evaluateJavascript("${callbackFnName}(${Gson().toJson(response?.body())});", ValueCallback(){}) })
                }

                override fun onFailure(call: Call<OcrResponse>, t: Throwable) {

                    progress.dismiss()
                    val res = OcrResponse(
                        resultMsg ="응답 없음",
                        resultCd = 888,
                        resultMsgTyp = "E",
                        data = null
                    )

                    if (t is SocketTimeoutException || t is IOException) {
                        webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}(${Gson().toJson(res)});")})
                    } else{
                        // 실패
                        webview.post(Runnable { webview.loadUrl("javascript:${callbackFnName}(${Gson().toJson(call)});")})
                    }

                }
            })

        } catch (e: Exception) {
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
}
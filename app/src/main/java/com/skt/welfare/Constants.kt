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
    val frontDevUrl = "http://welfaredevm.sktelecom.com:50000"
    val frontPrdUrl = "https://welfarem.sktelecom.com:50443"
    val frontStgUrl = "http://welfaredevm.sktelecom.com:50000"

    var frontUrl = ""

    val backendDevUrl = "http://welfaredevm.sktelecom.com:50001"
    val backendPrdUrl = "https://welfarem.sktelecom.com:58443"
    val backendStgUrl = "http://welfaredevm.sktelecom.com:50001"

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
    val toktokStgUrl = "https://m.toktok.sk.com:9443"
    val toktokPrdUrl = "https://m.toktok.sk.com:9443"

    val toktokStorePhonePackageName = "com.skt.pe.activity.mobileclient"
    val toktokPhonePackageName = "com.skt.pe.provider"
    val toktokStoreTabletPackageName = "com.sk.tablet.group.store"
    val toktokTabletPackageName = "com.skt.tablet.group.login"

    val toktokPhoneActionName = "com.sk.pe.group.auth.GMP_LOGIN"
    val toktokTabletActionName = "com.sk.pe.auth.GMP_LOGIN"

    val vacationPackageName = "com.gmp.skt.dywt.mobile"
    val vacationAppId = "Z000ST0049";

    val storeDevUrl = "https://m.toktok.sk.com/fordev.jsp"
    val storePrdUrl = "https://m.toktok.sk.com"

    var phoneNumber = ""

    var token = ""

    var loginInfo: TokTokResponse? = null

    var tmaptapi: TMapTapi? = null




}
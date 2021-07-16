package com.skt.welfare

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.*
import android.os.*
import android.provider.MediaStore
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.skt.welfare.api.TokTokApi
import com.skt.welfare.api.TokTokResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.NetworkInterface
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


var splashView: View? = null
var wrap_content : View? = null

var back_cnt = 0

val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
val STORAGE_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
val PHONE_PERMISSION = arrayOf(Manifest.permission.READ_PHONE_STATE)
val FLAG_PERMISSION_CAMERA = 1
val FLAG_PERMISSION_STORAGE = 2
val FLAG_PERMISSION_PHONE = 3
private lateinit var context : Context
private lateinit var mWebView : WebView

var webviewReloadFlag = false

private const val TAG = "MainActivity"
var filePathCallbackLollipop: ValueCallback<Array<Uri>>? = null
const val FILECHOOSER_REQ_CODE = 2002
const val TOKTOK_REQ_CODE = 1007
const val IMAGE_REQ_CODE = 1000
private var cameraImageUri: Uri? = null

private var authFlag = false



class MainActivity : AppCompatActivity() {

    //뒤로가기 연속 클릭 대기 시간
    private var mBackWait:Long = 0
    private var mBackWaitthird:Long = 0


    lateinit var webView : WebView;

    lateinit var retryBtn : Button;


    private var downloadId:Long = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this;
//        splashView = findViewById(R.id.view)

//        val mWebView : WebView = findViewById(R.id.web_view)



        authFlag = false

        var storePackagename = if(isTablet(context)) Constants.toktokStoreTabletPackageName else Constants.toktokStorePhonePackageName
        var toktokPackagename = if(isTablet(context)) Constants.toktokTabletPackageName else Constants.toktokPhonePackageName
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(Constants.storeUrl)
        /*
        //스토어 설치 체크
        if(!checkInstallationOf(context, storePackagename)) {
            startActivity(i)
            finish()
        }
        //톡톡 설치 체크
        else if(!checkInstallationOf(context, toktokPackagename)) {
            startActivity(i)
            finish()
        }
*/




        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            Constants.loginInfo = TokTokResponse(deviceToken = task.result.toString())

        })


        mWebView = findViewById(R.id.web_view)
        webView = findViewById(R.id.web_view)
        wrap_content = findViewById(R.id.wrap_content)

        retryBtn = findViewById(R.id.retry_btn)

        mWebView.webViewClient = WebViewClient()// 클릭시 새창 안뜨게
        mWebView.addJavascriptInterface(Bridge(this), Constants.javascriptBridgeName); // 자바스크립트 브릿지 연결
        if(Build.VERSION.SDK_INT >= 21) {//http 이미지 허용
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {

                if(message.contains("backKey is not defined")){
                    // 뒤로가기 버튼 클릭
                    if(mWebView.canGoBack()){
                        mWebView.goBack()
                    }
                    else if(System.currentTimeMillis() - mBackWait >=2000 ) {
                        mBackWait = System.currentTimeMillis()
                        Toast.makeText(context, Constants.appCloseWaitText, Toast.LENGTH_SHORT).show()
                    } else {
                        //액티비티 종료
                        finish()
                    }
                }

            }


            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams): Boolean {

                // Callback 초기화
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop?.onReceiveValue(null)
                    filePathCallbackLollipop = null
                }
                filePathCallbackLollipop = filePathCallback

                getFiles(fileChooserParams.isCaptureEnabled,1, fileChooserParams)
                return true

            }

        }

        //파일 다운로드
        mWebView.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            var downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            var _contentDisposition = URLDecoder.decode(contentDisposition, "UTF-8")
            var _mimetype = mimetype
            // 파일명 정제 시작
            var fileName = _contentDisposition.replace("attachment; filename=", "")
            if (!TextUtils.isEmpty(fileName)) {
                _mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimetype)
                if (fileName.endsWith(";")) { fileName = fileName.substring(0, fileName.length - 1) }
                if (fileName.startsWith("\"") && fileName.endsWith("\"")) { fileName = fileName.substring(1, fileName.length - 1) }
            }
            // 파일명 정제 끝

            var request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(_mimetype)
                addRequestHeader("User-Agent", userAgent)
                setDescription("Downloading File")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                setTitle(fileName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { setRequiresCharging(false) }
                allowScanningByMediaScanner()
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            }
            registerDownloadReceiver(downloadManager, this)
            downloadId = downloadManager.enqueue(request)
            Toast.makeText(context, "다운로드 시작", Toast.LENGTH_SHORT).show()

        })

        if(BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true);



        val mWebSettings : WebSettings = mWebView.settings //세부 세팅 등록
        mWebSettings.javaScriptEnabled = true // 웹페이지 자바스클비트 허용 여부
        mWebSettings.setSupportMultipleWindows(true) // 새창 띄우기 허용 여부
        mWebSettings.javaScriptCanOpenWindowsAutomatically = true // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.loadWithOverviewMode = true // 메타태그 허용 여부
        mWebSettings.useWideViewPort = true // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false) // 화면 줌 허용 여부
        mWebSettings.builtInZoomControls = false // 화면 확대 축소 허용 여부
        mWebSettings.cacheMode = WebSettings.LOAD_NO_CACHE // 브라우저 캐시 허용 여부
        mWebSettings.domStorageEnabled = true // 로컬저장소 허용 여부
        mWebSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // 브라우저 캐시 허용 여부
        mWebSettings.domStorageEnabled = true



        //권한체크
        val cameraPermiossion = checkPermission(CAMERA_PERMISSION, FLAG_PERMISSION_CAMERA)
        if(cameraPermiossion){
            val storagePermission = checkPermission(STORAGE_PERMISSION, FLAG_PERMISSION_STORAGE)
            if(storagePermission) {
                val phonePermission = checkPermission(PHONE_PERMISSION, FLAG_PERMISSION_PHONE)
                if(phonePermission) goMain(Constants.baseUrl)
            }
        }


        //네트워크 재시도
        retryBtn.setOnClickListener {
            if(getNetworkConnected()){
                val msg = handler.obtainMessage()
                val data = Bundle()
                data.putBoolean("network",true)
                msg.data = data
                handler.sendMessage(msg)
            }
            else{
                Toast.makeText(context, "네트워크 연결 안됨", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun registerDownloadReceiver(downloadManager: DownloadManager, activity: MainActivity) {
        var downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                var id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                when (intent?.action) {
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                        if(downloadId == id){
                            val query: DownloadManager.Query = DownloadManager.Query()
                            query.setFilterById(id)
                            var cursor = downloadManager.query(query)
                            if (!cursor.moveToFirst()) return
                            var columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            var status = cursor.getInt(columnIndex)
                            if (status == DownloadManager.STATUS_SUCCESSFUL) Toast.makeText(context, "다운로드가 완료됐습니다.", Toast.LENGTH_SHORT).show()
                            else if (status == DownloadManager.STATUS_FAILED) Toast.makeText(context, "다운로드가 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                }

            }
        }


        IntentFilter().run {
            addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            activity.registerReceiver(downloadReceiver, this)
        }
    }

    fun goMain(url: String){
        authFlag = true

        if(!authFlag){
            var actionName = if(isTablet(context)) Constants.toktokTabletActionName else Constants.toktokPhoneActionName
            val intent = Intent(actionName)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivityForResult(intent, TOKTOK_REQ_CODE)
        }
        else{
            mWebView.run {
                webViewClient = CustomWebViewClient()
                loadUrl(url + "?t=" + System.currentTimeMillis())
            }
        }



    }
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    override fun onBackPressed() {

        if(mWebView.visibility == View.VISIBLE){
            back_cnt++
            if(System.currentTimeMillis() - mBackWaitthird >=1000  && back_cnt > 1) {
                back_cnt = 0
            } else {
                if(back_cnt >= 3){
                    Toast.makeText(context, Constants.appCloseText, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            mBackWaitthird = System.currentTimeMillis()

            mWebView.post(Runnable { mWebView.loadUrl("javascript:backKey();") })
        }
        else{
            if(System.currentTimeMillis() - mBackWait >=2000 ) {
                mBackWait = System.currentTimeMillis()
                Toast.makeText(context, Constants.appCloseWaitText, Toast.LENGTH_SHORT).show()
            } else {
                //액티비티 종료
                finish()
            }

        }


    }


    /**
     * 권한 체크
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {

            FLAG_PERMISSION_CAMERA -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, Constants.cameraPermissionText, Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        checkPermission(STORAGE_PERMISSION, FLAG_PERMISSION_STORAGE)
                    }
                }
            }
            FLAG_PERMISSION_STORAGE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, Constants.storagePermissionText, Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        checkPermission(PHONE_PERMISSION, FLAG_PERMISSION_PHONE)
                    }
                }
            }
            FLAG_PERMISSION_PHONE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, Constants.phonePermissionText, Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        goMain(Constants.baseUrl)
                    }
                }
            }
        }
    }
    fun checkPermission(permissions: Array<out String>, flag: Int): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, flag)
                if(flag == 2) makeDir()
                return false
            }

        }
        return true
    }

    fun makeDir(){
//        val filePath = Environment.getExternalStorageDirectory().absolutePath + "/" + Constants.ocrImgFolder
//        if (!File(filePath).exists()) {
//            File(filePath).mkdir()
//        }
    }

    /**
     * 네트워크 체크
     */
    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // 네트워크가 연결
        }

        override fun onLost(network: Network) {
            // 네트워크가 끊김
            val msg = handler.obtainMessage()
            val data = Bundle()
            data.putBoolean("network",false)
            msg.data = data
            handler.sendMessage(msg)


        }
    }
    // 콜백을 등록하는 함수
    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallBack)
    }

    // 콜백을 해제하는 함수
    private fun terminateNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.unregisterNetworkCallback(networkCallBack)
    }

    override fun onResume() {
        super.onResume()
        registerNetworkCallback()
    }

    override fun onStop() {
        super.onStop()
        terminateNetworkCallback()
    }


    private fun getFiles(_isCapture: Boolean, selectedType: Int, fileChooserParams: WebChromeClient.FileChooserParams) {
        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val path = Environment.getExternalStorageDirectory()
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val fileName = "welfare_camera_${timeStamp}.png"
        val file = File(path, fileName)
        cameraImageUri = Uri.fromFile(file)
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        if (!_isCapture) {
            val pickIntent = Intent(Intent.ACTION_PICK)
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, fileChooserParams.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE)

            if (selectedType == 1) {
                pickIntent.type = MediaStore.Images.Media.CONTENT_TYPE
                pickIntent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if (selectedType == 2) {
                pickIntent.type = MediaStore.Video.Media.CONTENT_TYPE
                pickIntent.data = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            val pickTitle = ""
            val chooserIntent = Intent.createChooser(pickIntent, pickTitle)


            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(intentCamera))
            startActivityForResult(context as Activity, chooserIntent, FILECHOOSER_REQ_CODE, null)
        } else {
            startActivityForResult(context as Activity, intentCamera, FILECHOOSER_REQ_CODE, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var data = data
        when (requestCode) {

            TOKTOK_REQ_CODE -> if (resultCode == RESULT_OK) {
                val map = getAuthKeyCompanyCDEncPwdMDN(context)
                val companyCd = map?.get("COMPANY_CD")
                val encPwd = URLDecoder.decode(map?.get("ENC_PWD"),"UTF-8")
                val osName = "Android"
                val groupCd = "SK"
                val osVersion = android.os.Build.VERSION.SDK_INT
                val authKey = map?.get("AUTHKEY")
                val mdn = map?.get("MDN")
                val appId = Constants.toktokAppId
                val appVer = BuildConfig.VERSION_NAME
                val lang = Locale.getDefault().language


                val api = TokTokApi.create()
                api.auth("COMMON_COMMON_EMPINFO",companyCd,appId, appVer, encPwd, osName,groupCd,lang,authKey,osVersion, mdn).enqueue(object :
                    Callback<TokTokResponse> {
                        override fun onResponse(
                            call: Call<TokTokResponse>,
                            response: Response<TokTokResponse>
                        ) {


                            Constants.loginInfo = response.body()?.copy(
                                deviceToken = Constants.loginInfo!!.deviceToken
                            )


                            val email = response.body()?.email
                            val result = response.body()?.result
                            val resultMessage = response.body()?.resultMessage
                            if(result.equals("1000")){
                                authFlag = true
                                goMain(Constants.baseUrl)
                            }
                            else{
                                when(result){
                                    "7000","7001","7005","7008","7009","7015" -> {
                                        var actionName = if(isTablet(context)) Constants.toktokTabletActionName else Constants.toktokPhoneActionName
                                        val intent = Intent(actionName)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        Toast.makeText(context, "${result}-${resultMessage}", Toast.LENGTH_SHORT).show()
                                        startActivityForResult(intent, TOKTOK_REQ_CODE)
                                    }
                                    "3601","3602","3603" -> {
                                        Toast.makeText(context, "${result}-${resultMessage}", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    "7002","7010","7003","7006","7011","7012" -> {
                                        Toast.makeText(context, "${result}-${resultMessage}", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse(Constants.storeUrl)
                                        startActivity(intent)
                                        finish()
                                    }
                                    "3205" -> {//인증앱업데이트
                                        val installUrl = if(isTablet(context)) "toktok://com.sk.tablet.group.store.detail?appId=${Constants.toktokAppId}"
                                        else "toktok://com.skt.pe.activity.mobileclient.detail?appId=${Constants.toktokAppId}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(installUrl))
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_NO_HISTORY
                                        startActivity(intent)
                                        finish()
                                    }
                                    else -> {
                                        Toast.makeText(context, "${result}-${resultMessage}", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }
                            }
                        }
                        override fun onFailure(call: Call<TokTokResponse>, t: Throwable) {
                            Log.e(TAG, t.stackTraceToString())
                            Toast.makeText(context, t.stackTraceToString(), Toast.LENGTH_SHORT).show()
                            finish()
                        }
                })

            }
            IMAGE_REQ_CODE -> if (resultCode == RESULT_OK) {

            }
            FILECHOOSER_REQ_CODE -> if (resultCode == RESULT_OK) {
                if (filePathCallbackLollipop == null) return
                if (data == null) data = Intent()
                if (data.data == null) data.data = cameraImageUri

                if(data.clipData != null){
                    val count = data.clipData?.itemCount!!
                    val uris = Array<Uri>(count){
                        data.clipData?.getItemAt(it)?.uri!!
                    }
                    filePathCallbackLollipop!!.onReceiveValue(uris)
                }
                else{
                    filePathCallbackLollipop!!.onReceiveValue(
                        WebChromeClient.FileChooserParams.parseResult(
                            resultCode,
                            data
                        )
                    )
                }

                filePathCallbackLollipop = null
            } else {
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop!!.onReceiveValue(null)
                    filePathCallbackLollipop = null
                }

            }
            else -> {
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}
fun isTablet(context: Context): Boolean {
    return context.resources.configuration.smallestScreenWidthDp >= 600
}
//앱설치 체크
fun checkInstallationOf(context : Context, packagename : String) : Boolean{
    val pm = context.packageManager

    return try{
        pm.getPackageInfo(packagename, PackageManager.GET_META_DATA)
        true
    }
    catch(e : PackageManager.NameNotFoundException){
        false
    }
}

//연동파라미터 구하기
fun getAuthKeyCompanyCDEncPwdMDN(context : Context) : Map<String, String>?{
    var map  = HashMap<String, String>()
    val values = ContentValues()
    values.put("APPID", Constants.toktokAppId)
    val cr = context.contentResolver
    if(isTablet(context)){//테블릿
        val uri = cr.insert(Uri.parse("content://com.skt.pe.group.auth/GMP_AUTH_PWD"), values)
        val authValues = uri!!.pathSegments
        val returnId = authValues.get(1)
        if("E001".equals(returnId) || "E002".equals(returnId) || "E007".equals(returnId) || "E008".equals(returnId)){
            throw Exception("Auth Exception")
        }
        map.put("AUTHKEY", authValues.get(2))
        map.put("COMPANY_CD", authValues.get(3))
        map.put("ENC_PWD", URLEncoder.encode(authValues.get(4), "UTF-8"))
        map.put("MDN", authValues.get(5))
        return map
    }
    else{
        values.put("MDN", getMdn(context))

        val uri = cr.insert(Uri.parse("content://com.skt.pe.auth/GMP_AUTH_PWD"), values)
        val authValues = uri!!.pathSegments
        val returnId = authValues.get(1)
        if("E001".equals(returnId) || "E002".equals(returnId) || "E007".equals(returnId) || "E008".equals(returnId)){
            throw Exception("Auth Exception")
        }
        val buffer = authValues.get(2)
        var b_offset = 0
        var offset = buffer.indexOf("|")
        if(offset != -1){
            map.put("AUTHKEY", buffer.substring(0, offset))
            b_offset = offset
            offset = buffer.indexOf("|", offset + 1)
            if(offset != -1){
                map.put("COMPANY_CD", buffer.substring(b_offset+1, offset))
                map.put("ENC_PWD", URLEncoder.encode(buffer.substring(offset+1), "UTF-8"))
                map.put("MDN", getMdn(context))
                return map
            }
            else{
                return null
            }
        }
        else{
            return null
        }
    }
}


@SuppressLint("MissingPermission")
fun getMdn(context: Context) : String{
    var mdn = ""
    val cr = context.contentResolver
    if(isTablet(context)){//테블릿
        val uri = cr.insert(Uri.parse("content://com.skt.pe.auth/MACADDR"), ContentValues())
        val authValues= uri!!.pathSegments
        if(authValues.size > 0){
            mdn = authValues.get(2).toUpperCase()
        }
        else{
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for(nif in  all){
                if(!nif.name.equals("wlan0", ignoreCase = true)) continue

                val macBytes = nif.hardwareAddress

                if (macBytes == null){
                    mdn = ""
                }
                val res1 = StringBuffer()
                for(b in macBytes){
                    res1.append(Integer.toHexString(b.toInt() and 0xFF) + ":")
                }
                if(res1.length >0){
                    res1.deleteCharAt(res1.length -1)
                }
                mdn = res1.toString()
            }

        }
    }
    else{

        val uri = cr.insert(Uri.parse("content://com.skt.pe.auth/MDNCHK"), ContentValues())
        val authValues= uri!!.pathSegments
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if(authValues.size > 0){
            mdn = authValues.get(2)
        }
        else{
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            mdn = tm.line1Number

        }
    }

    Constants.phoneNumber = mdn
    return mdn

}

// 인터넷 연결 확인 함수
fun getNetworkConnected(): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork : NetworkInfo? = cm.activeNetworkInfo
    val isConnected : Boolean = activeNetwork?.isConnectedOrConnecting == true

    return isConnected

}

val handler: Handler = object : Handler() {
    @SuppressLint("HandlerLeak")
    override fun handleMessage(msg: Message?) {


        val anim = AlphaAnimation(0f, 1f);
        anim.duration = 500

        if(msg?.data!!.getBoolean("network")){
            wrap_content?.visibility = View.GONE
            mWebView?.visibility = View.VISIBLE
            if(webviewReloadFlag){
                mWebView?.reload()
                webviewReloadFlag = false
            }

        }
        else{
            webviewReloadFlag = true
            wrap_content?.visibility = View.VISIBLE
            mWebView?.visibility = View.GONE
        }

    }
}

class CustomWebViewClient : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {

        if(getNetworkConnected()){
            Handler().postDelayed({

                val msg = handler.obtainMessage()
                val data = Bundle()
                data.putBoolean("network",true)
                msg.data = data
                handler.sendMessage(msg)

            }, 500)
        }
        else{
            val msg = handler.obtainMessage()
            val data = Bundle()
            data.putBoolean("network",false)
            msg.data = data
            handler.sendMessage(msg)
        }


        super.onPageFinished(view, url)
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        super.onLoadResource(view, url)
    }

    override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
    }

    override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
    ): WebResourceResponse? {
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        return super.shouldOverrideUrlLoading(view, request)
    }
}


package com.skt.welfare

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.*
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


var splashView: View? = null
var wrap_content : View? = null

private const val TAG = "MainActivity"
var back_cnt = 0

val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
val STORAGE_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
val PHONE_PERMISSION = arrayOf(Manifest.permission.READ_PHONE_STATE)
val FLAG_PERMISSION_CAMERA = 1
val FLAG_PERMISSION_STORAGE = 2
val FLAG_PERMISSION_PHONE = 3
class MainActivity : AppCompatActivity() {

    //뒤로가기 연속 클릭 대기 시간
    private var mBackWait:Long = 0
    private var mBackWaitthird:Long = 0

    lateinit var mWebView : WebView
    private lateinit var context : Context



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this;

//        splashView = findViewById(R.id.view)

//        val mWebView : WebView = findViewById(R.id.web_view)

        mWebView = findViewById(R.id.web_view)
        wrap_content = findViewById(R.id.wrap_content)

        mWebView.webViewClient = WebViewClient()// 클릭시 새창 안뜨게
        mWebView.addJavascriptInterface(Bridge(this), Constants.javascriptBridgeName); // 자바스크립트 브릿지 연결

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
        }


        val mWebSettings : WebSettings = mWebView.settings //세부 세팅 등록
        mWebSettings.javaScriptEnabled = true // 웹페이지 자바스클비트 허용 여부
        mWebSettings.setSupportMultipleWindows(false) // 새창 띄우기 허용 여부
        mWebSettings.javaScriptCanOpenWindowsAutomatically = false // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.loadWithOverviewMode = true // 메타태그 허용 여부
        mWebSettings.useWideViewPort = true // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(true) // 화면 줌 허용 여부
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



//        val editText : TextInputEditText= findViewById(R.id.edit_text)
//        val button : Button= findViewById(R.id.button)
//        editText.setOnKeyListener{v, keyCode, event ->
//            v.hideKeyboard()
//            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KEYCODE_ENTER){
//                goMain(editText.text.toString())
//            }
//            true
//        }
//        button.setOnClickListener {
//            it.hideKeyboard()
//            goMain(editText.text.toString())
//        }



    }
    fun goMain(url : String ){
        mWebView.run {
            webViewClient = CustomWebViewClient()
            loadUrl(url)
        }
    }
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    override fun onBackPressed() {

        back_cnt++
        if(System.currentTimeMillis() - mBackWaitthird >=2000  && back_cnt > 1) {
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
    
    
    /**
     * 권한 체크
     */
    override fun onRequestPermissionsResult(requestCode: Int
                                            , permissions: Array<out String>
                                            , grantResults: IntArray) {
        when (requestCode) {
            FLAG_PERMISSION_CAMERA -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, Constants.cameraPermissionText, Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else{
                        checkPermission(STORAGE_PERMISSION, FLAG_PERMISSION_STORAGE)
                    }
                }
            }
            FLAG_PERMISSION_STORAGE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, Constants.storagePermissionText, Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else{
                        checkPermission(PHONE_PERMISSION, FLAG_PERMISSION_PHONE)
                    }
                }
            }
            FLAG_PERMISSION_PHONE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, Constants.phonePermissionText, Toast.LENGTH_LONG).show()
                        finish()
                    }
                    else{
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
                ActivityCompat.requestPermissions(this, permissions,  flag)
                return false
            }
        }
        return true
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
            val builder = AlertDialog.Builder(ContextThemeWrapper(this@MainActivity, R.style.Theme_AppCompat_Light_Dialog))
            builder.setTitle("")
            builder.setMessage("네트워크 끊김")
            builder.setCancelable(false)
            builder.setPositiveButton("다시시도") { _, _ ->
                if(!getNetworkConnected()){
                    onLost(network)
                }
            }
            builder.setNegativeButton("앱종료") { _, _ ->
                finish()
            }
            builder.show()
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

    fun getNetworkConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork : NetworkInfo? = cm.activeNetworkInfo
        val isConnected : Boolean = activeNetwork?.isConnectedOrConnecting == true

        return isConnected

    }
}

class CustomWebViewClient : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

    }

    override fun onPageFinished(view: WebView?, url: String?) {

        view?.visibility = View.VISIBLE
        wrap_content?.visibility = View.GONE

//        Handler().postDelayed({
//            splashView?.visibility = View.GONE
//        },500)

//        mainActivity?.setTheme(R.style.AppTheme)
//        splashView?.visibility = View.GONE

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


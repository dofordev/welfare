package com.skt.welfare

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


var splashView: View? = null



class MainActivity : AppCompatActivity() {

    //뒤로가기 연속 클릭 대기 시간
    private var mBackWait:Long = 0

    private lateinit var mWebView : WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        splashView = findViewById(R.id.view)

//        val mWebView : WebView = findViewById(R.id.web_view)

        mWebView = findViewById(R.id.web_view)

        mWebView.webViewClient = WebViewClient()// 클릭시 새창 안뜨게
        mWebView.addJavascriptInterface(Bridge(this), Constants.javascriptBridgeName); // 자바스크립트 브릿지 연결


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

        mWebView.run {
            webViewClient = CustomWebViewClient()
            loadUrl(Constants.baseUrl)

        }

    }

    override fun onBackPressed() {
//        mWebView.post(Runnable { mWebView.loadUrl("javascript:backKey();") })
        // 뒤로가기 버튼 클릭
        if(mWebView.canGoBack()){
            mWebView.goBack()
        }
        else if(System.currentTimeMillis() - mBackWait >=2000 ) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, Constants.mainCloseText, Toast.LENGTH_SHORT).show()
        } else {
            finish() //액티비티 종료
        }

    }

}




class CustomWebViewClient : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {


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

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
    ) {

        super.onReceivedError(view, request, error)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
    ): WebResourceResponse? {
        return super.shouldInterceptRequest(view, request)
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
    ): Boolean {
        return super.shouldOverrideUrlLoading(view, request)
    }
}
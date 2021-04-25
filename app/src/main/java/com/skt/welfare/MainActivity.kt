package com.skt.welfare

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText


var splashView: View? = null
var wrap_content : View? = null


class MainActivity : AppCompatActivity() {

    //뒤로가기 연속 클릭 대기 시간
    private var mBackWait:Long = 0

    private lateinit var mWebView : WebView
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
                        Toast.makeText(context, Constants.mainCloseText, Toast.LENGTH_SHORT).show()
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

        goMain(Constants.baseUrl)



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
        mWebView.post(Runnable { mWebView.loadUrl("javascript:backKey();") })


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
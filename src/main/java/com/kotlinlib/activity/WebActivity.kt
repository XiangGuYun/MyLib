package com.kotlinlib.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout

/**
 * 用于显示H5网页的Activity
 * @LayoutId(R.layout.activity_main)
 * class TextureViewCase1Activity : WebActivity() {
 * ·12=
 * override fun init(bundle: Bundle?) {
 * webUrl = "http://www.baidu.com"
 * initWebView(R.id.container, object :WebViewListener{
 * override fun onLoadResource(view: WebView?, url: String?) {
 * "load resource".logD("web")
 * }
 *
 * override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
 * "received error".logD("web")
 * }
 *
 * override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
 * "page started".logD("web")
 * }
 *
 * override fun onPageFinished(view: WebView?, url: String?) {
 * "page finished".logD("web")
 * }
 *
 * })
 * }
 *
 * }
 */
class WebActivity : KotlinActivity() {

    protected var webUrl: String? = null
    private var webView: WebView? = null
    private var webViewClient: WebViewClient? = null//网页客户端
    private var settings: WebSettings? = null//网页设置
    private var fl_web: FrameLayout? = null

    override fun init(bundle: Bundle?) {
        //重写init并调用initWebView();
    }

    protected fun initWebView(webContainerId: Int, listener: WebViewListener) {
        webView = WebView(this)
        val lp = FrameLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        webView!!.layoutParams = lp
        fl_web = findViewById(webContainerId)
        //有效果
        fl_web!!.addView(webView, 0)
        /*
        设置网页浏览器客户端
         */
        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
            }
        }
        /*
        设置网页视图客户端
         */
        initWebViewClient(listener)
        webView!!.settings.javaScriptEnabled = true
        webView!!.webViewClient = webViewClient
        //        webView.addJavascriptInterface(new AndroidToJS(this), "postData");
        //AndroidtoJS类对象映射到js的postData对象
        webView!!.loadUrl(webUrl)
        /*
        设置支持JS
         */
        settings = webView!!.settings
        // 设置可以支持缩放
        settings!!.setSupportZoom(true)
        // 设置出现缩放工具
        settings!!.builtInZoomControls = false
        //扩大比例的缩放
        settings!!.useWideViewPort = true
        //自适应屏幕
        settings!!.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings!!.loadWithOverviewMode = true
        settings!!.setAppCacheEnabled(true)
        settings!!.databaseEnabled = true
        settings!!.domStorageEnabled = true//开启DOM缓存，关闭的话H5自身的一些操作是无效的
        /*
        设置缓存模式
         */
        if (netOK()) {//判断是否有网络链接
            settings!!.cacheMode = WebSettings.LOAD_DEFAULT
        } else {
            settings!!.cacheMode = WebSettings.LOAD_CACHE_ONLY
        }
        /*！
        把图片加载放在最后来加载渲染
         */
        settings!!.blockNetworkImage = false
        /*
        设置渲染优先级
         */
        settings!!.setRenderPriority(WebSettings.RenderPriority.HIGH)
        /*
        支持多窗口
         */
        settings!!.setSupportMultipleWindows(true)
        /*
        开启 DOM storage API 功能
         */
        settings!!.domStorageEnabled = true
        /*
        开启 Application Caches 功能
         */
        settings!!.setAppCacheEnabled(true)
    }

    private fun initWebViewClient(listener: WebViewListener) {
        webViewClient = object : WebViewClient() {
            override fun onLoadResource(view: WebView, url: String) {
                //开始加载
                super.onLoadResource(view, url)
                listener.onLoadResource(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                webView!!.loadUrl(url) //设置不用系统浏览器打开,直接显示在当前Webview
                return false
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                listener.onReceivedError(view, errorCode, description, failingUrl)
                //                super.onReceivedError(view, errorCode, description, failingUrl);
                //                Log.d("Test", "网页错误");
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
                super.onPageStarted(view, url, favicon)
                listener.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                listener.onPageFinished(view, url)
            }
        }
    }

    override fun onDestroy() {
        if (webView != null) {
            webView!!.visibility = View.GONE
            webView!!.removeAllViews()
            webView!!.destroy()
        }
        fl_web!!.removeView(webView)
        super.onDestroy()
    }

    interface WebViewListener {
        fun onLoadResource(view: WebView, url: String)
        fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String)
        fun onPageStarted(view: WebView, url: String, favicon: Bitmap)
        fun onPageFinished(view: WebView, url: String)
    }

}







package com.arvix.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var appReady = false

    companion object {
        const val APP_URL = "https://arvix1.netlify.app/"
        val ALLOWED_DOMAINS = listOf(
            "arvix1.netlify.app", "arvix0.netlify.app",
            "supabase.co", "accounts.google.com",
            "google.com/o/oauth", "googleapis.com"
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { !appReady }
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.parseColor("#080f0d")
        val ic = WindowInsetsControllerCompat(window, window.decorView)
        ic.isAppearanceLightStatusBars = false
        ic.isAppearanceLightNavigationBars = false
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        setupWebView()
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
            appReady = true
        } else {
            if (isOnline()) webView.loadUrl(APP_URL)
            else { showOffline(); appReady = true }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
            builtInZoomControls = false
        }
        webView.settings.userAgentString += " ArvixApp/1.0"
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                appReady = true
            }
            override fun shouldOverrideUrlLoading(view: WebView, req: WebResourceRequest): Boolean {
                val url = req.url.toString()
                return if (ALLOWED_DOMAINS.any { url.contains(it) }) false
                else { try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (e: Exception) {}; true }
            }
            override fun onReceivedError(view: WebView?, req: WebResourceRequest?, err: WebResourceError?) {
                super.onReceivedError(view, req, err)
                if (req?.isForMainFrame == true) { showOffline(); appReady = true }
            }
        }
        webView.webChromeClient = WebChromeClient()
    }

    private fun showOffline() {
        webView.loadData(OFFLINE_HTML, "text/html; charset=utf-8", "UTF-8")
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onSaveInstanceState(out: Bundle) { super.onSaveInstanceState(out); webView.saveState(out) }
    override fun onKeyDown(kc: Int, ev: KeyEvent?): Boolean {
        if (kc == KeyEvent.KEYCODE_BACK && webView.canGoBack()) { webView.goBack(); return true }
        return super.onKeyDown(kc, ev)
    }
    override fun onResume() { super.onResume(); webView.onResume() }
    override fun onPause() { super.onPause(); webView.onPause() }
    override fun onDestroy() { webView.destroy(); super.onDestroy() }

    private val OFFLINE_HTML = """<!DOCTYPE html>
<html dir="rtl" lang="ar"><head><meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>*{margin:0;padding:0;box-sizing:border-box}
body{font-family:Arial,sans-serif;background:#080f0d;color:#e0f0ea;
display:flex;align-items:center;justify-content:center;
height:100vh;flex-direction:column;text-align:center;padding:24px}
.ico{font-size:64px;margin-bottom:20px}
h2{color:#12c887;font-size:20px;margin-bottom:10px;font-weight:800}
p{color:#4a7a65;font-size:14px;margin-bottom:28px}
button{padding:14px 32px;background:linear-gradient(135deg,#0d9e6e,#12c887);
color:#080f0d;border:none;border-radius:12px;font-size:15px;font-weight:800;cursor:pointer}
</style></head><body>
<div class="ico">📡</div>
<h2>لا يوجد إنترنت</h2>
<p>تحقق من اتصالك وأعد المحاولة</p>
<button onclick="location.reload()">⟳ إعادة المحاولة</button>
</body></html>""".trimIndent()
}

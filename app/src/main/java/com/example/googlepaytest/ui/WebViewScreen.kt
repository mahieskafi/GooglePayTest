package com.example.googlepaytest.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import org.json.JSONObject

class WebAppInterface(private val context: Context, private val webView: WebView) {
    @JavascriptInterface
    fun checkGooglePay() {
        val paymentsClient = Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build()
        )
        val isReadyToPayJson = JSONObject()
        isReadyToPayJson.put("apiVersion", 2)
        isReadyToPayJson.put("apiVersionMinor", 0)
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())
        val task: Task<Boolean> = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            val isReady = completedTask.result == true
            Toast.makeText(context, if (isReady) "Google Pay is ready!" else "Google Pay is not available.", Toast.LENGTH_LONG).show()
            if (isReady) {
                // Call JS to show Google Pay button
                (context as? Activity)?.runOnUiThread {
                    webView.evaluateJavascript("showGooglePayButton()", null)
                }
            }
        }
    }

    @JavascriptInterface
    fun startGooglePay() {
        // TODO: Implement Google Pay native flow here
        Toast.makeText(context, "Google Pay flow started! (implement native flow)", Toast.LENGTH_LONG).show()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            val iface = WebAppInterface(context, this)
            addJavascriptInterface(iface, "Android")
            loadUrl(url)
        }
    })
}

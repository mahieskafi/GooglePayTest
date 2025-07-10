package com.example.googlepaytest

import android.content.Intent
import android.os.Bundle
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.example.googlepaytest.ui.WebViewScreen
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData

class MainActivity : ComponentActivity() {
    companion object {
        const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    }
    private var webView: android.webkit.WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    com.example.googlepaytest.ui.WebViewScreen(
                        url = "file:///android_asset/webview_test.html",
                        onRequestGooglePay = { paymentDataRequestJson ->
                            val request = com.google.android.gms.wallet.PaymentDataRequest.fromJson(paymentDataRequestJson)
                            val paymentsClient = com.google.android.gms.wallet.Wallet.getPaymentsClient(
                                this,
                                com.google.android.gms.wallet.Wallet.WalletOptions.Builder().setEnvironment(com.google.android.gms.wallet.WalletConstants.ENVIRONMENT_TEST).build()
                            )
                            val task = paymentsClient.loadPaymentData(request)
                            AutoResolveHelper.resolveTask(task, this, LOAD_PAYMENT_DATA_REQUEST_CODE)
                        },
                        onWebViewReady = { wv -> webView = wv }
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val paymentData = PaymentData.getFromIntent(data!!)
                    val token = paymentData?.paymentMethodToken?.token
                    // Pass token to WebView via JS
                    webView?.post {
                        webView?.evaluateJavascript("onGooglePaySuccess('${token}')", null)
                    }
                }
                RESULT_CANCELED -> {
                    // Payment canceled
                }
                AutoResolveHelper.RESULT_ERROR -> {
                    // Handle error
                }
            }
        }
    }
}

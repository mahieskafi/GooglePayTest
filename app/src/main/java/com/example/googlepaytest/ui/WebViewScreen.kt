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

class WebAppInterface(private val context: Context, private val webView: WebView, private val onRequestGooglePay: (String) -> Unit) {
    @JavascriptInterface
    fun checkGooglePay() {
        val paymentsClient = Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build()
        )
        val isReadyToPayJson = JSONObject()
        isReadyToPayJson.put("apiVersion", 2)
        isReadyToPayJson.put("apiVersionMinor", 0)
        // Add allowedPaymentMethods for Google Pay validation
        val allowedPaymentMethods = org.json.JSONArray()
        val cardPaymentMethod = org.json.JSONObject()
        cardPaymentMethod.put("type", "CARD")
        val parameters = org.json.JSONObject()
        parameters.put("allowedAuthMethods", org.json.JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS"))
        parameters.put("allowedCardNetworks", org.json.JSONArray().put("AMEX").put("DISCOVER").put("JCB").put("MASTERCARD").put("VISA"))
        cardPaymentMethod.put("parameters", parameters)
        cardPaymentMethod.put("tokenizationSpecification", org.json.JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", org.json.JSONObject().apply {
                put("gateway", "example")
                put("gatewayMerchantId", "exampleGatewayMerchantId")
            })
        })
        allowedPaymentMethods.put(cardPaymentMethod)
        isReadyToPayJson.put("allowedPaymentMethods", allowedPaymentMethods)
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
        val paymentDataRequestJson = org.json.JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            val allowedPaymentMethods = org.json.JSONArray()
            val cardPaymentMethod = org.json.JSONObject()
            cardPaymentMethod.put("type", "CARD")
            val parameters = org.json.JSONObject()
            parameters.put("allowedAuthMethods", org.json.JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS"))
            parameters.put("allowedCardNetworks", org.json.JSONArray().put("AMEX").put("DISCOVER").put("JCB").put("MASTERCARD").put("VISA"))
            parameters.put("billingAddressRequired", true)
            val billingAddressParameters = org.json.JSONObject()
            billingAddressParameters.put("format", "FULL")
            parameters.put("billingAddressParameters", billingAddressParameters)
            cardPaymentMethod.put("parameters", parameters)
            cardPaymentMethod.put("tokenizationSpecification", org.json.JSONObject().apply {
                put("type", "PAYMENT_GATEWAY")
                put("parameters", org.json.JSONObject().apply {
                    put("gateway", "example")
                    put("gatewayMerchantId", "exampleGatewayMerchantId")
                })
            })
            allowedPaymentMethods.put(cardPaymentMethod)
            put("allowedPaymentMethods", allowedPaymentMethods)
            put("transactionInfo", org.json.JSONObject().apply {
                put("totalPriceStatus", "FINAL")
                put("totalPrice", "1.00")
                put("currencyCode", "USD")
            })
            put("merchantInfo", org.json.JSONObject().apply {
                put("merchantName", "Example Merchant")
            })
        }
        onRequestGooglePay(paymentDataRequestJson.toString())
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(url: String, onRequestGooglePay: (String) -> Unit, onWebViewReady: (WebView) -> Unit) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            settings.javaScriptEnabled = true
            val iface = WebAppInterface(context, this, onRequestGooglePay)
            addJavascriptInterface(iface, "Android")
            onWebViewReady(this)
            loadUrl(url)
        }
    })
}

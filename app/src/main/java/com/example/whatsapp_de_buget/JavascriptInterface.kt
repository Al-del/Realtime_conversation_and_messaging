package com.example.whatsapp_de_buget
import android.webkit.JavascriptInterface

class JavascriptInterface(val callActivity: CallActivity) {
    @JavascriptInterface
    public fun onPeerConnected() {

        callActivity.onPeerConnected()
    }
}
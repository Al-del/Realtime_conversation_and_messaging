package com.example.whatsapp_de_buget

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*


class CallActivity : AppCompatActivity() {


    var username = ""
    var friendsUsername = ""

    var isPeerConnected = true

    var firebaseRef = Firebase.database.getReference("users")

    var isAudio = true
    var isVideo = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        username = intent.getStringExtra("username")!!
        val callBtn:Button = findViewById<Button>(R.id.callBtn)
        val friendNameEdit:EditText = findViewById<EditText>(R.id.friendNameEdit)
     //   onPeerConnected()
        callBtn.setOnClickListener {
            friendsUsername = friendNameEdit.text.toString()
            sendCallRequest()
        }
        val toggleAudioBtn = findViewById<ImageView>(R.id.toggleAudioBtn)
        toggleAudioBtn.setOnClickListener {
            isAudio = !isAudio
            callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")")
            toggleAudioBtn.setImageResource(if (isAudio) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24)
        }
        val toggleVideoBtn = findViewById<ImageView>(R.id.toggleVideoBtn)
        toggleVideoBtn.setOnClickListener {
            isVideo = !isVideo
            callJavascriptFunction("javascript:toggleVideo(\"${isVideo}\")")
            toggleVideoBtn.setImageResource(if (isVideo) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24)
        }

        setupWebView()
    }

    private fun sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "You're not connected. Check your internet", Toast.LENGTH_LONG)
                .show()
            return
        }


    firebaseRef.child(friendsUsername).child("incoming").setValue(username)
        firebaseRef.child(friendsUsername).child("isAvailable").addValueEventListener(object:
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value.toString() == "true") {
                    listenForConnId()
                }

            }

        })

    }
    private fun listenForConnId() {
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null)
                    return
                switchToControls()
                callJavascriptFunction("javascript:startCall(\"${snapshot.value}\")")
            }

        })
    }

    private fun setupWebView() {
        val webView:WebView= findViewById<WebView>(R.id.webView)

        webView.webChromeClient = object: WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.addJavascriptInterface(JavascriptInterface(this), "Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
        val webView:WebView= findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true

//set the file path for the html file in the assets folder
        val settings: WebSettings = webView.getSettings()
        settings.allowFileAccess = true
        val filePath = "file:///android_asset/index.html"
        webView.loadUrl(filePath)

        webView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                initializePeer()
            }
        }
    }

    var uniqueId = ""
    private fun initializePeer() {

        uniqueId = getUniqueID()

        callJavascriptFunction("javascript:init(\"${uniqueId}\")")
        firebaseRef.child(username).child("incoming").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                onCallRequest(snapshot.value as? String)
            }

        })

    }
    private fun onCallRequest(caller: String?) {
        val callLayout:RelativeLayout = findViewById<RelativeLayout>(R.id.callLayout)
        val incomingCallTxt:TextView = findViewById<TextView>(R.id.incomingCallTxt)
        val acceptBtn:ImageView = findViewById<ImageView>(R.id.acceptBtn)
        val rejectBtn: ImageView= findViewById<ImageView>(R.id.rejectBtn)

        if (caller == null) return

        callLayout.visibility = View.VISIBLE
        incomingCallTxt.text = "$caller is calling..."

        acceptBtn.setOnClickListener {
            firebaseRef.child(username).child("connId").setValue(uniqueId)
            firebaseRef.child(username).child("isAvailable").setValue(true)

            callLayout.visibility = View.GONE
            switchToControls()
        }

        rejectBtn.setOnClickListener {
            firebaseRef.child(username).child("incoming").setValue(null)
            callLayout.visibility = View.GONE
        }

    }
    private fun switchToControls() {
        val inputLayout: RelativeLayout = findViewById<RelativeLayout>(R.id.inputLayout)
        val callControlLayout:LinearLayout = findViewById<LinearLayout>(R.id.callControlLayout)
        inputLayout.visibility = View.GONE
        callControlLayout.visibility = View.VISIBLE
    }
    private fun callJavascriptFunction(functionString: String) {
        val webView:WebView= findViewById<WebView>(R.id.webView)

        webView.post { webView.evaluateJavascript(functionString, null) }
    }
    fun onPeerConnected() {
        isPeerConnected = true
    }
    override fun onBackPressed() {
        finish()
    }
    override fun onDestroy() {
        val webView:WebView= findViewById<WebView>(R.id.webView)
        firebaseRef.child(username).setValue(null)
        webView.loadUrl("about:blank")
        super.onDestroy()
    }
    private fun getUniqueID(): String {
        return UUID.randomUUID().toString()
    }

}
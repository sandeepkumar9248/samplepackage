package com.sampleunittest.sdkcreation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.sampleunittest.mylibrary.ActionCallBack
import com.sampleunittest.mylibrary.InAppListener
import com.sampleunittest.mylibrary.InAppSDK
import org.webrtc.SurfaceViewRenderer

class InApiVcActivity : AppCompatActivity(), View.OnClickListener, InAppListener {

    //Views
    private lateinit var callerView: LinearLayout
    private lateinit var receiverView: LinearLayout
    lateinit var vcView: RelativeLayout
    private var isAudioMuted: Boolean = false
    private var isVideoMuted: Boolean = false
    private lateinit var muteVideo: AppCompatButton
    private lateinit var muteAudio: AppCompatButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_api_vc)

        val mLocalSurfaceView = findViewById<SurfaceViewRenderer>(R.id.LocalSurfaceView)
        val mRemoteSurfaceView = findViewById<SurfaceViewRenderer>(R.id.RemoteSurfaceView)


        //calling view
        val answerBtn: Button = findViewById(R.id.answerBtn)
        val declineBtn: Button = findViewById(R.id.declineBtn)
        val endCallBtn: Button = findViewById(R.id.endCallBtn)

        callerView = findViewById(R.id.callerView)
        receiverView = findViewById(R.id.receiverView)
        vcView = findViewById(R.id.vcView)


        val endCall = findViewById<AppCompatButton>(R.id.endCall)
        val switchCamera = findViewById<AppCompatButton>(R.id.switchCamera)
        muteVideo = findViewById(R.id.muteVideo)
        muteAudio = findViewById(R.id.muteAudio)
        val ansCall = findViewById<AppCompatButton>(R.id.ansCall)

        endCall.setOnClickListener(this)
        switchCamera.setOnClickListener(this)
        muteVideo.setOnClickListener(this)
        muteAudio.setOnClickListener(this)
        ansCall.setOnClickListener(this)
        answerBtn.setOnClickListener(this)
        declineBtn.setOnClickListener(this)
        endCallBtn.setOnClickListener(this)

        InAppSDK.initialise(applicationContext, mLocalSurfaceView, mRemoteSurfaceView, true, true)
        InAppSDK.instaListener(this)

        if (intent.getStringExtra("remoteId")?.isEmpty() != true) {
            Toast.makeText(this, "called", Toast.LENGTH_SHORT).show()
            callerView.visibility = View.GONE
            receiverView.visibility = View.GONE
            vcView.visibility = View.VISIBLE
            InAppSDK.makeCall(
                intent.getStringExtra("remoteId"),
                object : ActionCallBack {
                    override fun onSuccess(message: String?) {
                        Log.d("onSuccess", message!!)

                    }

                    override fun onFailure(error: String?) {
                        Log.d("onSuccess", "error makeCall call" + error!!)
                    }
                })
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.declineBtn -> {
                InAppSDK.disconnect()
            }
            R.id.endCallBtn -> {
                InAppSDK.disconnect()
            }

            R.id.muteAudio -> {
                if (isAudioMuted) {
                    isAudioMuted = false
                    muteAudio.text = "Mute Audio"
                    InAppSDK.audioUnMute()
                } else {
                    isAudioMuted = true
                    muteAudio.text = "UnMute Audio"
                    InAppSDK.audioMute()
                }

            }
            R.id.muteVideo -> {
                if (isVideoMuted) {
                    isVideoMuted = false
                    muteVideo.text = "Mute Video"
                    InAppSDK.videoUnMute()
                } else {
                    isVideoMuted = true
                    InAppSDK.videoMute()
                    muteVideo.text = "UnMute Video"
                }
            }
            R.id.switchCamera -> {
                InAppSDK.switchCamera()
            }
            R.id.endCall -> {
                InAppSDK.leave()
            }
            R.id.answerBtn -> {
                InAppSDK.answerCall(object : ActionCallBack {
                    override fun onSuccess(message: String?) {
                        Log.d("onSuccess", message!!)
                        receiverView.visibility = View.GONE
                        callerView.visibility = View.GONE
                        vcView.visibility = View.VISIBLE
                    }

                    override fun onFailure(error: String?) {
                        Log.d("onSuccess", error!!)
                    }
                })
            }
        }
    }

    override fun offerReceived(remoteId: String?) {
        runOnUiThread {
            Log.d("onSuccess", "offerReceived erre$remoteId")
            receiverView.visibility = View.VISIBLE
            callerView.visibility = View.GONE
            vcView.visibility = View.GONE
        }
    }

    override fun onFinished() {
        finish()
    }

    override fun remoteUserDisconnected() {
        Log.d("onSuccess", "remoteUserDisconnected")

    }
}
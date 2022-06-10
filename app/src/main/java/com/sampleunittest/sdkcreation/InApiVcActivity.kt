package com.sampleunittest.sdkcreation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import com.sampleunittest.mylibrary.ActionCallBack
import com.sampleunittest.mylibrary.InAppListener
import com.sampleunittest.mylibrary.InAppSDK
import org.webrtc.SurfaceViewRenderer

class InApiVcActivity : AppCompatActivity(), View.OnClickListener, InAppListener {

    //Views
    private lateinit var callerView: LinearLayout
    private lateinit var receiverView: LinearLayout
    private lateinit var enterRemoteIdView: LinearLayout
    lateinit var vcView: RelativeLayout
    private var isAudioMuted: Boolean = false
    private var isVideoMuted: Boolean = false
    private lateinit var muteVideo: AppCompatImageView
    private lateinit var muteAudio: AppCompatImageView
    private lateinit var remoteIdEt: AppCompatEditText
    private var remoteId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_api_vc)

        val mLocalSurfaceView = findViewById<SurfaceViewRenderer>(R.id.LocalSurfaceView)
        val mRemoteSurfaceView = findViewById<SurfaceViewRenderer>(R.id.RemoteSurfaceView)


        //calling view
        val answerBtn: Button = findViewById(R.id.answerBtn)
        val declineBtn: Button = findViewById(R.id.declineBtn)
        val endCallBtn: Button = findViewById(R.id.endCallBtn)
        val startCallBtn: Button = findViewById(R.id.startCallBtn)

        callerView = findViewById(R.id.callerView)
        receiverView = findViewById(R.id.receiverView)
        vcView = findViewById(R.id.vcView)
        enterRemoteIdView = findViewById(R.id.enterRemoteIdView)
        remoteIdEt = findViewById(R.id.remoteId)


        val endCall = findViewById<AppCompatImageView>(R.id.endCall)
        val switchCamera = findViewById<AppCompatImageView>(R.id.switchCamera)
        muteVideo = findViewById(R.id.muteVideo)
        muteAudio = findViewById(R.id.muteAudio)

        endCall.setOnClickListener(this)
        switchCamera.setOnClickListener(this)
        muteVideo.setOnClickListener(this)
        muteAudio.setOnClickListener(this)
        answerBtn.setOnClickListener(this)
        declineBtn.setOnClickListener(this)
        endCallBtn.setOnClickListener(this)
        startCallBtn.setOnClickListener(this)

        InAppSDK.initialise(applicationContext, mLocalSurfaceView, mRemoteSurfaceView, false, true)
        InAppSDK.instaListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.startCallBtn -> {
                remoteId = remoteIdEt.text.toString()
                if (remoteId.isNotEmpty()) {
                    callerView.visibility = View.VISIBLE
                    enterRemoteIdView.visibility = View.GONE
                    Log.d("remoteId", "onClick: $remoteId")
                    InAppSDK.makeCall(remoteId,
                        object : ActionCallBack {
                            override fun onSuccess(message: String?) {
                                runOnUiThread {
                                    Log.d("onSuccess", message!!)
                                }
                            }

                            override fun onFailure(error: String?) {
                                Log.d("onSuccess", "error makeCall call" + error!!)
                            }
                        })
                }

            }
            R.id.declineBtn -> {endCall()
            }
            R.id.endCallBtn -> {endCall()
            }

            R.id.muteAudio -> {
                if (isAudioMuted) {
                    isAudioMuted = false
                    muteAudio.setImageResource(R.drawable.ic_baseline_mic_24)
                    InAppSDK.audioUnMute()
                } else {
                    isAudioMuted = true
                    muteAudio.setImageResource(R.drawable.ic_baseline_mic_off_24)
                    InAppSDK.audioMute()
                }

            }
            R.id.muteVideo -> {
                if (isVideoMuted) {
                    isVideoMuted = false
                    muteVideo.setImageResource(R.drawable.ic_baseline_videocam_24)
                    InAppSDK.videoUnMute()
                } else {
                    isVideoMuted = true
                    InAppSDK.videoMute()
                    muteVideo.setImageResource(R.drawable.ic_baseline_videocam_off_24)
                }
            }
            R.id.switchCamera -> {
                InAppSDK.switchCamera()
            }
            R.id.endCall -> {endCall()
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
            Log.d("onSuccess", "offerReceived from $remoteId")
            receiverView.visibility = View.VISIBLE
            callerView.visibility = View.GONE
            vcView.visibility = View.GONE
            enterRemoteIdView.visibility = View.GONE
        }
    }

    override fun answerReceived(message: String?) {
        runOnUiThread {
            Log.d("onSuccess", "Call $message")
            receiverView.visibility = View.GONE
            callerView.visibility = View.GONE
            vcView.visibility = View.VISIBLE
            enterRemoteIdView.visibility = View.GONE
        }

    }

    override fun onFinished() {
        finish()
    }

    override fun remoteUserDisconnected() {
        finish()
    }

    private fun endCall(){
        InAppSDK.disconnect(object : ActionCallBack {
            override fun onSuccess(message: String?) {

                Log.d("onSuccess", message!!)
            }

            override fun onFailure(error: String?) {
                Log.d("onSuccess", error!!)
            }
        })
    }
}
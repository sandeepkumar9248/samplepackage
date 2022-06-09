package com.sampleunittest.sdkcreation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.sampleunittest.mylibrary.ActionCallBack
import com.sampleunittest.mylibrary.InAppListener
import com.sampleunittest.mylibrary.InAppSDK

class CallingActivity : AppCompatActivity(), View.OnClickListener, InAppListener {

    lateinit var callerView: LinearLayout
    private lateinit var receiverView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)

        val answerBtn: Button = findViewById(R.id.answerBtn)
        val declineBtn: Button = findViewById(R.id.declineBtn)
        val endCallBtn: Button = findViewById(R.id.endCallBtn)
        callerView = findViewById(R.id.callerView)
        receiverView = findViewById(R.id.receiverView)

        InAppSDK.instaListener(this)


        if (intent.getStringExtra("remoteId")!!.isNotEmpty()) {
            callerView.visibility = View.VISIBLE

            startActivity(Intent(this@CallingActivity, InApiVcActivity::class.java)
                .putExtra("remoteId", intent.getStringExtra("remoteId")))
        }

        answerBtn.setOnClickListener(this)
        declineBtn.setOnClickListener(this)
        endCallBtn.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.answerBtn -> {
                startActivity(
                    Intent(this@CallingActivity, InApiVcActivity::class.java)
                        .putExtra("answerCall", true)
                        .putExtra("remoteId", "")
                )
            }
            R.id.declineBtn -> {
                InAppSDK.disconnect()
            }
            R.id.endCallBtn -> {
                InAppSDK.disconnect()
            }
        }
    }

    override fun offerReceived(remoteId: String?) {
        runOnUiThread {
            Log.d("onSuccess", "offerReceived call$remoteId")

            receiverView.visibility = View.VISIBLE
        }
    }

    override fun onFinished() {

    }

    override fun remoteUserDisconnected() {

    }

}
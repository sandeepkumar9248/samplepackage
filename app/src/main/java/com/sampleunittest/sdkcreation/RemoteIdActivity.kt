package com.sampleunittest.sdkcreation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import com.sampleunittest.mylibrary.ActionCallBack
import com.sampleunittest.mylibrary.InAppSDK

class RemoteIdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_id)

        val remoteId: AppCompatEditText = findViewById(R.id.remoteId)
        val startCallBtn: Button = findViewById(R.id.startCallBtn)

        startCallBtn.setOnClickListener {
            startActivity(
                Intent(this@RemoteIdActivity, InApiVcActivity::class.java)
                    .putExtra("remoteId", remoteId.text.toString())
            )
        }
    }
}
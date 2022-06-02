package com.sampleunittest.sdkcreation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.sampleunittest.mylibrary.ToasterMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch {

            Log.d("hello", "onCreate: " + ToasterMessage().getDataFromLibrary(this@MainActivity, "helo"))
        }
    }
}
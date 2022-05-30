package com.sampleunittest.mylibrary

import android.content.Context
import android.widget.Toast




class ToasterMessage {

    fun showToast(c: Context?, message: String?) {
        Toast.makeText(c, message, Toast.LENGTH_SHORT).show()
    }
}
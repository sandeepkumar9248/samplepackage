package com.sampleunittest.mylibrary

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@DelicateCoroutinesApi
class ToasterMessage {

    suspend fun getDataFromLibrary(): List<DataItem?>? {
        var usersResponse: List<DataItem?>? = arrayListOf()

        val value = GlobalScope.async {
            withContext(Dispatchers.IO) {
                val apiInterface = ApiInterface.create().getMovies()
                val response: Response<UsersResponse> = apiInterface.execute()
                usersResponse = response.body().data
                Log.d("Error", "onFailure: $usersResponse")
            }

        }
        value.await()


        return usersResponse!!
    }
}
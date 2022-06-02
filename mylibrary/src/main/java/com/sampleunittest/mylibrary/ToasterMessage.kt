package com.sampleunittest.mylibrary

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ToasterMessage {

    suspend fun getDataFromLibrary(c: Context?, message: String?): List<DataItem?>? {
//        Toast.makeText(c, message, Toast.LENGTH_SHORT).show()
        var usersResponse: List<DataItem?>? = arrayListOf()

        val value = GlobalScope.async {
            val apiInterface = ApiInterface.create().getMovies()
            val response: Response<UsersResponse> = apiInterface.execute()
            usersResponse = response.body().data
            Log.d("Error", "onFailure: ${usersResponse}")

        }
        println("value =  ${value.await()} thread running on [${Thread.currentThread().name}]")


        //apiInterface.enqueue( Callback<List<Movie>>())


//        val apiResponse: UsersResponse = response.body()
//        apiInterface.enqueue(object : Callback<UsersResponse> {
//            override fun onResponse(
//                call: Call<UsersResponse>?,
//                response: Response<UsersResponse>?
//            ) {
//                Log.d("Error", "onFailure: ${response?.message()}")
//
//                if (response?.body() != null) {
//                    usersResponse = response.body().data
//                    Log.d("Error", "onFailure: ${usersResponse}")
//
//                }
//            }
//
//            override fun onFailure(call: Call<UsersResponse>?, t: Throwable?) {
//                Log.d("Error", "onFailure: ${t!!.message}")
//            }
//        })


        return usersResponse!!
    }
}
package com.sampleunittest.mylibrary

import android.content.Context
import android.graphics.Movie
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ToasterMessage {

    fun getDataFromLibrary(c: Context?, message: String?): UsersResponse {
        Toast.makeText(c, message, Toast.LENGTH_SHORT).show()
        var usersResponse: UsersResponse? = null
        val apiInterface = ApiInterface.create().getMovies()

        //apiInterface.enqueue( Callback<List<Movie>>())
        apiInterface.enqueue(object : Callback<UsersResponse> {
            override fun onResponse(
                call: Call<UsersResponse>?,
                response: Response<UsersResponse>?
            ) {

                if (response?.body() != null) {
                    usersResponse = response.body()
                }
            }

            override fun onFailure(call: Call<UsersResponse>?, t: Throwable?) {

            }
        })

        return usersResponse!!

    }
}
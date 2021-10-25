package com.skt.welfare

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView
import com.skt.welfare.api.BackendApi
import com.skt.welfare.api.ImageRequest
import com.skt.welfare.api.ImageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "ImageActivity"
private lateinit var context : Context
class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        context = this;
        val photoView : PhotoView = findViewById(R.id.photoView)



        val headerTitle : TextView = findViewById(R.id.headerTitle)
        headerTitle.text = intent.getStringExtra("fileName")

        val api = BackendApi.create()
        api.postImage(
            ImageRequest(
            filePath = intent.getStringExtra("filePath")
        )
        ).enqueue(object : Callback<ImageResponse> {
            override fun onResponse(
                call: Call<ImageResponse>,
                response: Response<ImageResponse>
            ) {
                val encodeByte: ByteArray = Base64.decode(response?.body()?.data, Base64.DEFAULT)

                photoView.setImageBitmap(BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size))

            }
            override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                Log.e(TAG, t.stackTraceToString())
                Toast.makeText(context, "서버 에러", Toast.LENGTH_SHORT).show()
            }
        })


        val closeBtn : ImageView = findViewById(R.id.closeBtn)

        closeBtn.setOnClickListener{

            val intent = Intent();
            setResult(RESULT_OK, intent);
            finish()
        }



    }
}
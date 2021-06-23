package com.skt.welfare

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView


class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val photoView : PhotoView = findViewById(R.id.photoView)

        val encodeByte: ByteArray = Base64.decode(intent.getStringExtra("imageBase64"), Base64.DEFAULT)

        photoView.setImageBitmap(BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size))

        val headerTitle : TextView = findViewById(R.id.headerTitle)
        headerTitle.text = intent.getStringExtra("fileName")


        val closeBtn : ImageView = findViewById(R.id.closeBtn)

        closeBtn.setOnClickListener{

            val intent = Intent();
            setResult(RESULT_OK, intent);
            finish()
        }
    }
}
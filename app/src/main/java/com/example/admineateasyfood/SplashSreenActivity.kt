package com.example.admineateasyfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashSreenActivity : AppCompatActivity() {
    // Phương thức onCreate được gọi khi Activity được tạo ra
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Thiết lập giao diện người dùng cho Activity từ tệp layout activity_splash_sreen.xml
        setContentView(R.layout.activity_splash_sreen)

        // Tạo một Handler để thực hiện một tác vụ sau một khoảng thời gian trễ
        Handler(Looper.getMainLooper()).postDelayed({
            // Tạo một Intent để chuyển từ SplashSreenActivity sang LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            // Bắt đầu Activity mới
            startActivity(intent)
            // Kết thúc SplashSreenActivity để người dùng không thể quay lại
            finish()
        }, 3000) // Độ trễ là 3000 mili giây
    }
}

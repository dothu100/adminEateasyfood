package com.example.admineateasyfood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.admineateasyfood.databinding.ActivitySignUpBinding
import com.example.admineateasyfood.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class SignUpActivity : AppCompatActivity() {

    // Khai báo biến
    private lateinit var userName: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // Biến binding sử dụng ViewBinding
    private val binding: ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Khởi tạo Firebase Auth
        auth = Firebase.auth

        // Khởi tạo tham chiếu đến cơ sở dữ liệu Firebase
        database = Firebase.database.reference

        // Bắt sự kiện khi click vào nút "Tạo tài khoản"
        binding.createUserButton.setOnClickListener {
            // Lấy thông tin từ các trường nhập
            userName = binding.name.text.toString().trim()
            email = binding.emailOrPhone.text.toString().trim()
            password = binding.password.text.toString().trim()

            // Kiểm tra và yêu cầu điền đầy đủ thông tin
            if (userName.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Gọi hàm tạo tài khoản
                createAccount(email, password)
            }
        }

        // Bắt sự kiện khi click vào nút "Đã có tài khoản"
        binding.alreadyHaveAccountButton.setOnClickListener {
            val intent = Intent(this, LoginActivity:: class.java)
            startActivity(intent)
        }

    }

    // Hàm tạo tài khoản mới bằng email và mật khẩu
    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Nếu tạo tài khoản thành công, hiển thị thông báo và chuyển đến trang đăng nhập
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                saveUserData()
                val intent = Intent(this, LoginActivity:: class.java)
                startActivity(intent)
                finish()
            } else {
                // Nếu tạo tài khoản thất bại, hiển thị thông báo lỗi và ghi log
                Toast.makeText(this, "Account creation failed", Toast.LENGTH_SHORT).show()
                Log.d("Account", "CreateAccount failed", task.exception)
            }
        }
    }

    // Hàm lưu thông tin người dùng vào cơ sở dữ liệu Firebase
    private fun saveUserData() {
        // Lấy thông tin từ các trường nhập
        userName = binding.name.text.toString().trim()
        email = binding.emailOrPhone.text.toString().trim()
        password = binding.password.text.toString().trim()

        // Tạo đối tượng UserModel từ thông tin người dùng
        val user = UserModel(userName, email, password)

        // Lấy ID của người dùng hiện tại và lưu thông tin vào cơ sở dữ liệu Firebase
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database.child("user").child(userId).setValue(user)
    }
}

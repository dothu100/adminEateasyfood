package com.example.admineateasyfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.admineateasyfood.databinding.ActivityCreateUserBinding
import com.example.admineateasyfood.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class CreateUserActivity : AppCompatActivity() {

    private lateinit var userName: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // Biến binding sử dụng ViewBinding
    private val binding: ActivityCreateUserBinding by lazy {
        ActivityCreateUserBinding.inflate(layoutInflater)
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
        binding.createNewUser.setOnClickListener {
            // Lấy thông tin từ các trường nhập
            userName = binding.name.text.toString().trim()
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            // Kiểm tra và yêu cầu điền đầy đủ thông tin
            if (userName.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Gọi hàm tạo tài khoản
                createAccount(email, password)
            }
        }
        binding.backButton.setOnClickListener {
            finish()
        }

    }

    // Hàm tạo tài khoản mới bằng email và mật khẩu
    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Nếu tạo tài khoản thành công, hiển thị thông báo và chuyển đến trang đăng nhập
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                saveUserData()
            } else {
                // Nếu tạo tài khoản thất bại, hiển thị thông báo lỗi và ghi log
                Toast.makeText(this, "Account creation failed", Toast.LENGTH_SHORT).show()
                Log.d("Account", "CreateAccount failed", task.exception)
            }
        }
    }

    // Hàm lưu thông tin người dùng vào cơ sở dữ liệu Firebase
//    private fun saveUserData() {
//        // Lấy thông tin từ các trường nhập
//        userName = binding.name.text.toString().trim()
//        email = binding.email.text.toString().trim()
//        password = binding.password.text.toString().trim()
//
//        // Tạo đối tượng UserModel từ thông tin người dùng
//        val user = UserModel(userName, email, password)
//
//        // Lấy ID của người dùng hiện tại và lưu thông tin vào cơ sở dữ liệu Firebase
//        val userId = FirebaseAuth.getInstance().currentUser!!.uid
//        database.child("user").child(userId).setValue(user)
//    }
    private fun saveUserData() {
        // Lấy thông tin từ các trường nhập
        userName = binding.name.text.toString().trim()
        email = binding.email.text.toString().trim()
        password = binding.password.text.toString().trim()

        // Tạo đối tượng UserModel từ thông tin người dùng
        val user = UserModel(userName, email, password)

        // Lấy ID của người dùng hiện tại và lưu thông tin vào cơ sở dữ liệu Firebase
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        database.child("user").child(userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Nếu lưu thông tin thành công, xóa dữ liệu trong các trường nhập
                binding.name.text.clear()
                binding.email.text.clear()
                binding.password.text.clear()
            } else {
                // Nếu lưu thông tin thất bại, hiển thị thông báo lỗi và ghi log
                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                Log.d("SaveUserData", "Failed to save user data", task.exception)
            }
        }
    }

}
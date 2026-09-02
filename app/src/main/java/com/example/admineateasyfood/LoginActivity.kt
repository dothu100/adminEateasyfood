package com.example.admineateasyfood

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.admineateasyfood.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    // Khai báo biến
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Cấu hình tùy chọn đăng nhập Google
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Khởi tạo Firebase Auth và tham chiếu cơ sở dữ liệu Firebase
        auth = Firebase.auth
        database = Firebase.database.reference

        // Khởi tạo Google SignInClient
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Xử lý sự kiện khi click vào nút "Đăng nhập"
        binding.loginButton.setOnClickListener {
            // Lấy thông tin từ trường email và password
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            // Kiểm tra và xử lý nếu trường email hoặc password trống
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Gọi hàm đăng nhập hoặc tạo tài khoản mới
                signInAccount(email, password)
            }
        }

        // Xử lý sự kiện khi click vào nút "Đăng nhập bằng Google"
        binding.googleButton.setOnClickListener {
            // Tạo Intent để đăng nhập bằng Google
            val signInIntent = googleSignInClient.signInIntent
            // Khởi chạy activity để đăng nhập bằng Google
            launcher.launch(signInIntent)
        }

        // Xử lý sự kiện khi click vào nút "Chưa có tài khoản"
        binding.dontHaveButton.setOnClickListener {
            // Chuyển đến activity đăng ký
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    // Hàm xử lý đăng nhập hoặc tạo tài khoản mới
//    private fun signInOrCreateAccount(email: String, password: String) {
//        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { signInTask ->
//            if (signInTask.isSuccessful) {
//                // Đăng nhập thành công
//                val user = auth.currentUser
//                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
//                // Chuyển đến MainActivity
//                updateUi(user)
//            } else {
//                // Đăng nhập thất bại, thử tạo tài khoản mới
//                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { createUserTask ->
//                    if (createUserTask.isSuccessful) {
//                        // Tạo tài khoản mới thành công
//                        val user = auth.currentUser
//                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
//                        // Chuyển đến MainActivity
//                        updateUi(user)
//                    } else {
//                        // Đăng nhập thất bại, hiển thị thông báo lỗi
//                        Toast.makeText(this, "Account does not exist!", Toast.LENGTH_SHORT).show()
//                        Log.d("Account", "Login failed. Please create a new account.", createUserTask.exception)
//                    }
//                }
//            }
//        }
//    }
    private fun signInAccount(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
//                    createUserWithEmailAndPassword(email, password)
                    Toast.makeText(this, "Account does not exist!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Hàm xử lý kết quả khi đăng nhập bằng Google
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Nếu kết quả trả về thành công
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                // Lấy thông tin tài khoản Google
                val account: GoogleSignInAccount? = task.result
                // Tạo credential từ token của tài khoản Google
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                // Đăng nhập vào Firebase bằng credential
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Đăng nhập thành công, chuyển đến MainActivity
                        Toast.makeText(this, "Google Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Đăng nhập thất bại, hiển thị thông báo lỗi
                        Toast.makeText(this, "Google Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // Nếu kết quả trả về không thành công, hiển thị thông báo
            Toast.makeText(this, "Google Login failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm kiểm tra đăng nhập trước đó khi activity bắt đầu
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser!= null) {
            // Nếu đã đăng nhập, chuyển đến MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // Hàm chuyển đến MainActivity khi đăng nhập thành công
    private fun updateUI(user: FirebaseUser?) {
        Toast.makeText(this, "Sign-In successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

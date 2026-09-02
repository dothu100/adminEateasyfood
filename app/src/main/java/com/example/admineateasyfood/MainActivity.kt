package com.example.admineateasyfood

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.admineateasyfood.databinding.ActivityMainBinding
import com.example.admineateasyfood.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    // Sử dụng lazy để khởi tạo biến binding
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    // Khai báo biến database để truy cập Firebase Database
    private lateinit var database: FirebaseDatabase
    // Khai báo biến auth để xác thực người dùng với Firebase
    private lateinit var auth: FirebaseAuth
    // Khai báo biến completeOrderReference để tham chiếu tới đơn hàng hoàn thành trong Firebase Database
    private lateinit var completeOrderReference: DatabaseReference

    // Phương thức onCreate được gọi khi Activity được tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bật chế độ Edge-to-Edge
        enableEdgeToEdge()
        // Thiết lập nội dung cho Activity từ binding
        setContentView(binding.root)

        // Khởi tạo đối tượng FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Thiết lập sự kiện khi nhấn vào nút logout
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Thiết lập các nút và sự kiện nhấp
        setupUI()

        // Gọi hàm để xử lý các đơn hàng
        pendingOrder()
        completeOrder()
        wholeTimeEarning()
    }

    // Phương thức hiển thị hộp thoại xác nhận đăng xuất
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to LogOut?")
            .setPositiveButton("Yes") { dialog, which ->
                // Đăng xuất và chuyển đến LoginActivity
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Phương thức lấy và hiển thị số lượng đơn hàng đang chờ xử lý
    private fun pendingOrder() {
        // Khởi tạo đối tượng FirebaseDatabase
        database = FirebaseDatabase.getInstance()
        // Tham chiếu đến nút "OrderDetails" trong cơ sở dữ liệu
        val pendingOrderReference = database.reference.child("OrderDetails")

        // Thêm một sự kiện lắng nghe dữ liệu từ Firebase
        pendingOrderReference.addListenerForSingleValueEvent(object : ValueEventListener {
            // Phương thức được gọi khi dữ liệu thay đổi
            override fun onDataChange(snapshot: DataSnapshot) {
                // Đếm số lượng phần tử con
                val pendingOrderItemCount = snapshot.childrenCount.toInt()
                // Cập nhật TextView với số lượng đơn hàng đang chờ xử lý
                binding.pendingOrder.text = "$pendingOrderItemCount"
            }

            // Phương thức được gọi khi có lỗi xảy ra
            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
                showToast("Failed to load pending orders: ${error.message}")
            }
        })
    }

    // Phương thức tính toán tổng thu nhập từ các đơn hàng đã hoàn thành
    private fun wholeTimeEarning() {
        // Tạo danh sách lưu trữ tổng số tiền của các đơn hàng đã hoàn thành
        val listOfTotalPay = mutableListOf<Int>()
        // Tham chiếu đến nút "CompletedOrder" trong cơ sở dữ liệu
        completeOrderReference = FirebaseDatabase.getInstance().reference.child("CompletedOrder")
        // Thêm một sự kiện lắng nghe dữ liệu từ Firebase
        completeOrderReference.addListenerForSingleValueEvent(object : ValueEventListener {
            // Phương thức được gọi khi dữ liệu thay đổi
            override fun onDataChange(snapshot: DataSnapshot) {
                // Lặp qua tất cả các phần tử con trong snapshot
                for (orderSnapShot in snapshot.children) {
                    val completeOrder = orderSnapShot.getValue(OrderDetails::class.java)
                    // Chỉ thêm giá trị nếu paymentReceived là true
                    if (completeOrder?.paymentReceived == true) {
                        completeOrder.totalPrice?.replace("$", "")?.toIntOrNull()?.let { totalPrice ->
                            listOfTotalPay.add(totalPrice)
                        }
                    }
                }
                // Cập nhật TextView với tổng thu nhập
                binding.wholeTimeEarning.text = "${listOfTotalPay.sum()}$"
            }

            // Phương thức được gọi khi có lỗi xảy ra
            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
                showToast("Failed to load completed orders: ${error.message}")
            }
        })
    }

    // Phương thức lấy và hiển thị số lượng đơn hàng đã hoàn thành
    private fun completeOrder() {
        // Tham chiếu đến nút "CompletedOrder" trong cơ sở dữ liệu
        val completeOrderReference = database.reference.child("CompletedOrder")
        var completeOrderItemCount = 0
        // Thêm một sự kiện lắng nghe dữ liệu từ Firebase
        completeOrderReference.addListenerForSingleValueEvent(object : ValueEventListener {
            // Phương thức được gọi khi dữ liệu thay đổi
            override fun onDataChange(snapshot: DataSnapshot) {
                // Đếm số lượng phần tử con
                completeOrderItemCount = snapshot.childrenCount.toInt()
                // Cập nhật TextView với số lượng đơn hàng đã hoàn thành
                binding.completedOrder.text = completeOrderItemCount.toString()
            }

            // Phương thức được gọi khi có lỗi xảy ra
            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load complete order: ${error.message}")
            }
        })
    }

    // Phương thức thiết lập các nút và sự kiện nhấp
    private fun setupUI() {
        binding.addMenu.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            startActivity(intent)
        }
        binding.allItemMenu.setOnClickListener {
            val intent = Intent(this, AllItemActivity::class.java)
            startActivity(intent)
        }
        binding.outForDeliveryButton.setOnClickListener {
            val intent = Intent(this, OutForDeliveryActivity::class.java)
            startActivity(intent)
        }
        binding.profile.setOnClickListener {
            val intent = Intent(this, AdminProfileActivity::class.java)
            startActivity(intent)
        }
        binding.createUser.setOnClickListener {
            val intent = Intent(this, CreateUserActivity::class.java)
            startActivity(intent)
        }
        binding.pendingOrderTextView.setOnClickListener {
            val intent = Intent(this, PendingOrderActivity::class.java)
            startActivity(intent)
        }
    }

    // Phương thức hiển thị thông báo Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

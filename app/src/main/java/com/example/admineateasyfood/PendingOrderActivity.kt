package com.example.admineateasyfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.admineateasyfood.adapter.PendingOrderAdapter
import com.example.admineateasyfood.databinding.ActivityPendingOrderBinding
import com.example.admineateasyfood.model.OrderDetails
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Định nghĩa lớp PendingOrderActivity kế thừa từ AppCompatActivity và triển khai giao diện PendingOrderAdapter.OnItemClicked
class PendingOrderActivity : AppCompatActivity(), PendingOrderAdapter.OnItemClicked {

    private lateinit var binding: ActivityPendingOrderBinding
    private var listOfName: MutableList<String> = mutableListOf()
    private var listOfTotalPrice: MutableList<String> = mutableListOf()
    private var listOfImageFirstFoodOrder: MutableList<String> = mutableListOf()
    private var listOfOrderItem: ArrayList<OrderDetails> = arrayListOf()
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseOrderDetails: DatabaseReference

    // Phương thức onCreate được gọi khi Activity được tạo ra
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sử dụng View Binding để gắn layout
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo FirebaseDatabase và DatabaseReference
        database = FirebaseDatabase.getInstance()
        databaseOrderDetails = database.reference.child("OrderDetails")

        // Gọi phương thức lấy dữ liệu đơn hàng
        getOrderDetails()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    // Phương thức lấy dữ liệu đơn hàng từ Firebase
    private fun getOrderDetails() {
        databaseOrderDetails.addListenerForSingleValueEvent(object : ValueEventListener {
            // Xử lý khi dữ liệu thay đổi
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfOrderItem.clear()
                // Lặp qua các phần tử trong snapshot và thêm vào danh sách đơn hàng
                for (orderSnapshot in snapshot.children) {
                    val orderDetails = orderSnapshot.getValue(OrderDetails::class.java)
                    orderDetails?.let {
                        listOfOrderItem.add(it)
                    }
                }
                // Gọi phương thức thêm dữ liệu vào danh sách cho RecyclerView
                addDataToListForRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // Phương thức thêm dữ liệu vào danh sách cho RecyclerView
    private fun addDataToListForRecyclerView() {
        listOfName.clear()
        listOfTotalPrice.clear()
        listOfImageFirstFoodOrder.clear()

        // Lặp qua danh sách đơn hàng và thêm dữ liệu vào các danh sách
        for (orderItem in listOfOrderItem) {
            orderItem.userName?.let {
                listOfName.add(it)
            }
            orderItem.totalPrice?.let {
                listOfTotalPrice.add(it)
            }
            orderItem.foodImages?.firstOrNull()?.let {
                listOfImageFirstFoodOrder.add(it)
            }
        }
        // Thiết lập adapter cho RecyclerView
        setAdapter()
    }

    // Phương thức thiết lập adapter cho RecyclerView
    private fun setAdapter() {
        binding.pendingOrderRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = PendingOrderAdapter(this, listOfName, listOfTotalPrice, listOfImageFirstFoodOrder, this)
        binding.pendingOrderRecyclerView.adapter = adapter
    }

    // Phương thức xử lý sự kiện khi mục trong RecyclerView được nhấn
    override fun onItemClickedListener(position: Int) {
        val intent = Intent(this, OrderDetailsActivity::class.java)
        val userOrderDetails = listOfOrderItem[position]
        intent.putExtra("UserOrderDetails", userOrderDetails)
        startActivity(intent)
    }

    // Phương thức xử lý sự kiện khi mục "Chấp nhận đơn hàng" được nhấn
    override fun onItemAcceptClickedListener(position: Int) {
        val childItemPushKey = listOfOrderItem[position].itemPushKey
        val clickItemOrderReference = childItemPushKey?.let {
            database.reference.child("OrderDetails").child(it)
        }
        clickItemOrderReference?.child("orderAccepted")?.setValue(true)?.addOnSuccessListener {
            updateOrderAcceptStatus(position)
        }
    }

    // Phương thức xử lý sự kiện khi mục "Giao đơn hàng" được nhấn
    override fun onItemDispatchClickedListener(position: Int) {
        val dispatchItemPushKey = listOfOrderItem[position].itemPushKey
        val dispatchItemOrderReference = database.reference.child("CompletedOrder").child(dispatchItemPushKey!!)
        dispatchItemOrderReference.setValue(listOfOrderItem[position])
            .addOnSuccessListener {
                deleteThisItemFromOrderDetails(dispatchItemPushKey)
            }
    }

    // Phương thức xóa đơn hàng khỏi OrderDetails sau khi đã giao
    private fun deleteThisItemFromOrderDetails(dispatchItemPushKey: String) {
        val orderDetailsItemsReference = database.reference.child("OrderDetails").child(dispatchItemPushKey)
        orderDetailsItemsReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Order is Dispatched", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Order is not Dispatched", Toast.LENGTH_SHORT).show()
            }
    }

    // Phương thức cập nhật trạng thái đơn hàng đã được chấp nhận
    private fun updateOrderAcceptStatus(position: Int) {
        val userIdOfClickedItem = listOfOrderItem[position].userUid
        val pushKeyOfClickedItem = listOfOrderItem[position].itemPushKey
        val buyHistoryReference = database.reference.child("user").child(userIdOfClickedItem!!).child("BuyHistory")
            .child(pushKeyOfClickedItem!!)
        buyHistoryReference.child("orderAccepted").setValue(true)
        databaseOrderDetails.child(pushKeyOfClickedItem).child("orderAccepted").setValue(true)
    }
}

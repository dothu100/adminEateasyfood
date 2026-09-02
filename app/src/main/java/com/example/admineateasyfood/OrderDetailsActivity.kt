package com.example.admineateasyfood

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.admineateasyfood.adapter.OrderDetailsAdapter
import com.example.admineateasyfood.databinding.ActivityOrderDetailsBinding
import com.example.admineateasyfood.model.OrderDetails

class OrderDetailsActivity : AppCompatActivity() {

    private val binding: ActivityOrderDetailsBinding by lazy {
        ActivityOrderDetailsBinding.inflate(layoutInflater)
    }

    private var userName: String ?= null
    private var address: String ?= null
    private var phoneNumber: String ?= null
    private var totalPrice: String ?= null
    private var foodNames: ArrayList<String> = arrayListOf()
    private var foodImages: ArrayList<String> = arrayListOf()
    private var foodQuantity: ArrayList<Int> = arrayListOf()
    private var foodPrices: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }
        getDataFromIntent()

    }

    private fun getDataFromIntent() {
        val recievedOrderDetails = intent.getSerializableExtra("UserOrderDetails") as OrderDetails

        recievedOrderDetails?. let {
                userName = recievedOrderDetails.userName
                foodNames = recievedOrderDetails.foodNames as ArrayList<String>
                foodImages = recievedOrderDetails.foodImages as ArrayList<String>
                foodQuantity = recievedOrderDetails.foodQuantities as ArrayList<Int>
                address = recievedOrderDetails.address
                phoneNumber = recievedOrderDetails.phoneNumber
                foodPrices = recievedOrderDetails.foodPrices as ArrayList<String>
                totalPrice = recievedOrderDetails.totalPrice

                setUserDetail()
                setAdapter()

        }

    }

    private fun setUserDetail() {
        binding.name.text = userName
        binding.address.text = address
        binding.phone.text = phoneNumber
        binding.totalPay.text = totalPrice
    }


    private fun setAdapter() {
        binding.orderDetailRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = OrderDetailsAdapter(this, foodNames, foodImages, foodQuantity, foodPrices)
        binding.orderDetailRecyclerView.adapter = adapter

    }

}
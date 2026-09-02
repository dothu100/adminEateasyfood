package com.example.admineateasyfood

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.admineateasyfood.databinding.ActivityAddItemBinding
import com.example.admineateasyfood.model.AllMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddItemBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var foodImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.AddItemButton.setOnClickListener {
            val foodName = binding.foodName.text.toString().trim()
            val foodPrice = binding.foodPrice.text.toString().trim()
            val foodDescription = binding.description.text.toString().trim()
            val foodIngredient = binding.ingredint.text.toString().trim()

            if (foodName.isNotBlank() && foodPrice.isNotBlank() && foodDescription.isNotBlank() && foodIngredient.isNotBlank()) {
                uploadData(foodName, foodPrice, foodDescription, foodIngredient)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.selectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun uploadData(foodName: String, foodPrice: String, foodDescription: String, foodIngredient: String) {
        val menuRef = database.getReference("menu")
        val newItemKey = menuRef.push().key

        val foodPriceNumber = foodPrice.toDoubleOrNull()
        if (foodPriceNumber == null || foodPriceNumber <= 10000) {
            showToast("Price must be greater than 10000")
            return
        }

        if (newItemKey == null) {
            showToast("Error creating item")
            return
        }

        if (foodImageUri == null) {
            showToast("Please select an image")
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("menu_image/$newItemKey.jpg")
        val uploadTask = imageRef.putFile(foodImageUri!!)

        uploadTask.addOnSuccessListener { _ ->
            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val newItem = AllMenu(
                    newItemKey,
                    foodName = foodName,
                    foodPrice = foodPrice,
                    foodDescription = foodDescription,
                    foodImage = downloadUrl.toString(),
                    foodIngredient = foodIngredient
                )

                menuRef.child(newItemKey).setValue(newItem)
                    .addOnSuccessListener {
                        showToast("Data uploaded successfully")
                        finish()
                    }
                    .addOnFailureListener {
                        showToast("Error uploading data: ${it.message}")
                    }
            }.addOnFailureListener {
                showToast("Error downloading image URL: ${it.message}")
            }
        }.addOnFailureListener {
            showToast("Error uploading image: ${it.message}")
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.selectedImage.setImageURI(it)
            foodImageUri = it
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        pickImage.unregister()
        super.onDestroy()
    }
}

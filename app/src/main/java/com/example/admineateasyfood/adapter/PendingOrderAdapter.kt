package com.example.admineateasyfood.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.admineateasyfood.PendingOrderActivity
import com.example.admineateasyfood.databinding.PendingOdersItemBinding


class PendingOrderAdapter(
    private val context: Context,
    private val customerNames: MutableList<String>,
    private val quantity: MutableList<String>,
    private val foodImages: MutableList<String>,
    private val itemClicked: OnItemClicked
):RecyclerView.Adapter<PendingOrderAdapter.PendingOrderViewHolder>() {

    interface OnItemClicked {
        fun onItemClickedListener(position: Int)
        fun onItemAcceptClickedListener(position: Int)
        fun onItemDispatchClickedListener(position: Int)

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PendingOrderViewHolder {
       val binding = PendingOdersItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PendingOrderViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PendingOrderViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = customerNames.size
    inner class PendingOrderViewHolder(private val binding: PendingOdersItemBinding):RecyclerView.ViewHolder(binding.root) {
        private var isAccepted = false
        fun bind(position: Int){
            binding.apply {
                customerName.text = customerNames[position]
                pendingOredarQuantity.text=quantity[position]
                var uriString = foodImages[position]
                var uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(foodImage)
                orderedAcceptButton.apply {
                    if(!isAccepted){
                        text="Accepted"
                    }else{
                        text="Dispatch"
                    }
                    setOnClickListener{
                        if (!isAccepted){
                            text="Dispatch"
                            isAccepted = true
                            showToast("Order is accepted")
                            itemClicked.onItemAcceptClickedListener(position)
                        }else{
                            customerNames.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            showToast("Order is dispatched ")
                            itemClicked.onItemDispatchClickedListener(position)
                        }

                    }
                }

                itemView.setOnClickListener {
                    itemClicked.onItemClickedListener(position)
                }

            }

        }
        private fun showToast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

}
package com.alfadev.bizlinker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class InvoiceProductAdapter(private val productList: ArrayList<InvoiceProductItem>, private val itemClickListener: OnItemClickListener): RecyclerView.Adapter<InvoiceProductAdapter.ProductViewHolder>() {
	interface OnItemClickListener {
		fun onItemClick(position: Int)
	}
	
	inner class ProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		val productNameTxt: TextView = itemView.findViewById(R.id.invoiceProductNameTxt)
		val productPriceTxt: TextView = itemView.findViewById(R.id.invoiceProductPrice)
		val productLogoTxt: TextView = itemView.findViewById(R.id.invoiceProductLogoTxt)
		val productIcon: ImageView = itemView.findViewById(R.id.imageViewInvoiceProduct)
		
		init {
			itemView.setOnClickListener {
				val position = bindingAdapterPosition
				if(position != RecyclerView.NO_POSITION) {
					itemClickListener.onItemClick(position)
				}
			}
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
		val view: View = LayoutInflater.from(parent.context).inflate(R.layout.invoice_product_item, parent, false)
		return ProductViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
		val productItem = productList[position]
		holder.productNameTxt.text = productItem.product.name
		holder.productPriceTxt.text = productItem.product.price.toString()
		if(productItem.product.image != null) Glide.with(holder.productIcon.context).load(productItem.product.image).into(holder.productIcon)
		else holder.productLogoTxt.text = productItem.product.name.substring(0, 2)
	}
	
	override fun getItemCount(): Int {
		return productList.size
	}
}
package com.alfadev.bizlinker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alfadev.bizlinker.ProductAdapter.OnLongItemClickListenerProduct

class WishlistAdapter(
	private val productList: ArrayList<WishlistItem>,
	private val itemClickListenerWishlist: OnItemClickListenerWishlist,
	private val longItemClickListenerWishlist: OnLongItemClickListenerWishlist
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {
	interface OnItemClickListenerWishlist {
		fun onItemClickWishlist(position: Int)
	}
	
	interface OnLongItemClickListenerWishlist {
		fun onLongItemClickWishlist(position: Int)
	}
	
	inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val productNameTxt: TextView = itemView.findViewById(R.id.invoiceProductNameTxt)
		val productLogoTxt: TextView = itemView.findViewById(R.id.invoiceProductLogoTxt)
		val productCount: TextView = itemView.findViewById(R.id.invoiceProductCount)
		val productPrice: TextView = itemView.findViewById(R.id.invoiceProductPrice)
		
		init {
			itemView.setOnClickListener {
				val position = bindingAdapterPosition
				if (position != RecyclerView.NO_POSITION) {
					itemClickListenerWishlist.onItemClickWishlist(position)
				}
			}
			itemView.setOnLongClickListener {
				val position = bindingAdapterPosition
				if (position != RecyclerView.NO_POSITION) {
					longItemClickListenerWishlist.onLongItemClickWishlist(position)
					true // Возвращаем true, чтобы указать, что событие обработано
				} else {
					false // Возвращаем false, если позиция некорректна
				}
			}
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
		val view: View = LayoutInflater.from(parent.context)
			.inflate(R.layout.invoice_product_item, parent, false)
		return WishlistViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
		val productItem = productList[position]
		holder.productNameTxt.text = productItem.name
		holder.productCount.text = productItem.count.toString()
		holder.productLogoTxt.text = productItem.name.substring(0, 2)
		holder.productPrice.text = productItem.price.toString()
	}
	
	override fun getItemCount(): Int {
		return productList.size
	}
}
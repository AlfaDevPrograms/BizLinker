package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductAdapter(private val productList: ArrayList<ProductItem>, private val itemClickListenerProduct: OnItemClickListenerProduct, private val longItemClickListenerProduct: OnLongItemClickListenerProduct): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
	interface OnItemClickListenerProduct {
		fun onItemClickProduct(position: Int)
	}
	interface OnLongItemClickListenerProduct {
		fun onLongIItemClickProduct(position: Int)
	}
	inner class ProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		val productNameTxt: TextView = itemView.findViewById(R.id.productNameTxt)
		val productDescriptionTxt: TextView = itemView.findViewById(R.id.productDescriptionTxt)
		val productLogoTxt: TextView = itemView.findViewById(R.id.productLogoTxt)
		val productCountTxt: TextView = itemView.findViewById(R.id.productCountTxt)
		val productPriceTxt: TextView = itemView.findViewById(R.id.productPriceTxt)
		val productLogo: ImageView = itemView.findViewById(R.id.imageViewProduct)
		
		init {
			itemView.setOnClickListener {
				val position = bindingAdapterPosition
				if(position != RecyclerView.NO_POSITION) {
					itemClickListenerProduct.onItemClickProduct(position)
				}
			}
			itemView.setOnLongClickListener {
				val position = bindingAdapterPosition
				if(position != RecyclerView.NO_POSITION) {
					longItemClickListenerProduct.onLongIItemClickProduct(position)
					true // Возвращаем true, чтобы указать, что событие обработано
				} else {
					false // Возвращаем false, если позиция некорректна
				}
			}
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
		val view: View = LayoutInflater.from(parent.context).inflate(R.layout.products_item, parent, false)
		return ProductViewHolder(view)
	}
	
	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
		val productItem = productList[position]
		holder.productNameTxt.text = productItem.name
		holder.productDescriptionTxt.text = productItem.description
		if(productItem.image != null){
			Glide.with(holder.productLogo.context).load(productItem.image).into(holder.productLogo)
			holder.productLogo.visibility = VISIBLE
			holder.productLogoTxt.visibility = INVISIBLE
		}
		else
		{
			holder.productLogoTxt.visibility = VISIBLE
			holder.productLogo.visibility = INVISIBLE
			holder.productLogoTxt.text = productItem.name.substring(0, 2)
		}
		holder.productCountTxt.text = productItem.count.toString()
		holder.productPriceTxt.text = productItem.price.toString()
	}
	
	override fun getItemCount(): Int {
		return productList.size
	}
}
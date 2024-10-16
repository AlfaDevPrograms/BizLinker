package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfadev.bizlinker.AddEditProductActivity.Companion.editProductItem
import com.alfadev.bizlinker.InvoiceActivity.Companion.hideKeyboard
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.databinding.ProductsFragmentBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProductsFragment: Fragment(), ProductAdapter.OnItemClickListenerProduct, WishlistAdapter.OnItemClickListenerWishlist {
	//Биндинг
	private lateinit var binding: ProductsFragmentBinding
	private lateinit var listProducts: ArrayList<ProductItem>
	private lateinit var listWishlist: ArrayList<WishlistItem>
	private lateinit var dialog: Dialog
	@SuppressLint("UseCompatLoadingForDrawables")
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = ProductsFragmentBinding.inflate(inflater, container, false)
		binding.products.layoutManager = LinearLayoutManager(this.context)
		binding.searchProducts.setOnClickListener {
			if(binding.productsHeaderTxt.visibility == VISIBLE) {
				binding.productsHeaderTxt.visibility = INVISIBLE
				binding.searchProductsET.visibility = VISIBLE
				binding.searchProductsET.requestFocus()
				val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
				imm?.showSoftInput(binding.searchProductsET, InputMethodManager.SHOW_IMPLICIT)
			}
			else {
				if(binding.searchProductsET.isFocused) {
					hideKeyboard(binding.searchProductsET)
				}
				else {
					binding.searchProductsET.requestFocus()
					val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
					imm?.showSoftInput(binding.searchProductsET, InputMethodManager.SHOW_IMPLICIT)
				}
				if(binding.searchProductsET.text.isNullOrEmpty()) {
					binding.productsHeaderTxt.visibility = VISIBLE
					binding.searchProductsET.visibility = INVISIBLE
				}
			}
		}
		binding.searchProductsET.setOnEditorActionListener { _, actionId, _ ->
			if(actionId == EditorInfo.IME_ACTION_SEARCH) {
				hideKeyboard(binding.searchProductsET)
				if(binding.searchProductsET.text.isNullOrEmpty()) {
					binding.productsHeaderTxt.visibility = VISIBLE
					binding.searchProductsET.visibility = INVISIBLE
				}
				true
			}
			else {
				false
			}
		}
		binding.searchProductsET.doAfterTextChanged {
			if(sharedPreferences.getString("role", "provider") == "provider") {
				val newList = listProducts.filter { it.name.lowercase().startsWith(binding.searchProductsET.text.toString().lowercase()) }
				binding.products.adapter = ProductAdapter(newList as ArrayList<ProductItem>, this)
			}
			else {
				val newList = listWishlist.filter { it.productName.lowercase().startsWith(binding.searchProductsET.text.toString().lowercase()) }
				binding.products.adapter = WishlistAdapter(newList as ArrayList<WishlistItem>, this)
			}
		}
		if(sharedPreferences.getString("role", "provider") == "provider") {
			listProducts = ArrayList()
			for(i in 0 .. 10) {
				listProducts.add(ProductItem(i.toLong(), getString(R.string.product_name_product_item_txt_hint) + " $i", 1000,getString(R.string.product_description_product_item_txt_hint) + " $i", null, 100.00))
			}
			binding.products.adapter = ProductAdapter(listProducts, this)
		}
		else {
			listWishlist = ArrayList()
			for(i in 0 .. 10) {
				listWishlist.add(WishlistItem(i.toLong(), getString(R.string.product_name_product_item_txt_hint) + " $i", getString(R.string.product_count_product_item_txt_hint).toInt() + i, 200.00,OrganizationItem(1.toLong(), getString(R.string.username_chat_item_txt_hint) + " $1", 1.toString(), 1.toString(), 1.toString(), 1.toString(), 1.toString(), 1.toString(), arrayListOf(EmailItem(1.toLong(), "")), arrayListOf(PhoneItem(1.toLong(), "")), null, OrganizationForm(1.toLong(), ""), null)))
			}
			binding.products.adapter = WishlistAdapter(listWishlist, this)
		}
		dialog = Dialog(requireActivity())
		dialog.setCancelable(true)
		dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialog.setContentView(R.layout.add_edit_wishlist)
		val name = dialog.findViewById<TextInputEditText>(R.id.addEditWishlistNameTIET)
		val count = dialog.findViewById<TextInputEditText>(R.id.addEditWishlistCountTIET)
		val price = dialog.findViewById<TextInputEditText>(R.id.addEditWishlistPriceTIET)
		val nameTxt = dialog.findViewById<TextInputLayout>(R.id.addEditWishlistNameTIL)
		val countTxt = dialog.findViewById<TextInputLayout>(R.id.addEditWishlistCountTIL)
		val priceTxt = dialog.findViewById<TextInputLayout>(R.id.addEditWishlistPriceTIL)
		val header = dialog.findViewById<TextView>(R.id.addEditWishlistHeaderTxt)
		name.doAfterTextChanged {
			if(it!!.isNotEmpty()) {
				nameTxt.error = null
			}
		}
		count.doAfterTextChanged {
			if(it!!.isNotEmpty()) {
				countTxt.error = null
			}
		}
		price.doAfterTextChanged {
			if(it!!.isNotEmpty()) {
				priceTxt.error = null
			}
		}
		val close = dialog.findViewById<ImageButton>(R.id.backToWishlistFromAddEditWishlist)
		val save = dialog.findViewById<Button>(R.id.buttonAddEditWishlistSave)
		save.setOnClickListener {
			if(name.text!!.isEmpty()) {
				nameTxt.error = getString(R.string.error_input)
			}
			if(count.text!!.isEmpty()) {
				countTxt.error = getString(R.string.error_input)
			}
			if(price.text!!.isEmpty()) {
				priceTxt.error = getString(R.string.error_input)
			}
			if(name.text!!.isNotEmpty() && count.text!!.isNotEmpty() && price.text!!.isNotEmpty()) {
				close.performClick()
			}
		}
		close.setOnClickListener {
			nameTxt.error = null
			countTxt.error = null
			name.setText("")
			count.setText("")
			price.setText("")
			name.clearFocus()
			count.clearFocus()
			price.clearFocus()
			dialog.dismiss()
		}
		binding.addProduct.setOnClickListener {
			if(sharedPreferences.getString("role", "provider") == "provider") {
				val myIntent = Intent(this@ProductsFragment.context, AddEditProductActivity::class.java)
				startActivity(myIntent)
			}
			else {
				header.text = getString(R.string.add_product)
				dialog.show()
			}
		}
		return binding.root
	}
	
	private var param1: String? = null
	private var param2: String? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			param1 = it.getString(ARG_PARAM1)
			param2 = it.getString(ARG_PARAM2)
		}
	}
	
	override fun onItemClickProduct(position: Int) {
		editProductItem = listProducts[position]
		val myIntent = Intent(this@ProductsFragment.context, AddEditProductActivity::class.java)
		startActivity(myIntent)
	}
	
	override fun onItemClickWishlist(position: Int) {
		val item = listWishlist[position]
		val header = dialog.findViewById<TextView>(R.id.addEditWishlistHeaderTxt)
		val name = dialog.findViewById<TextInputEditText>(R.id.addEditWishlistNameTIET)
		val count = dialog.findViewById<TextInputEditText>(R.id.addEditWishlistCountTIET)
		val price = dialog.findViewById<TextInputEditText>(R.id.addEditWishlistPriceTIET)
		name.setText(item.productName)
		count.setText(item.productCount.toString())
		price.setText(item.productPrice.toString())
		header.text = getString(R.string.edit_product)
		dialog.show()
	}
}

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
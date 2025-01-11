package com.alfadev.bizlinker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.databinding.ActivityInvoiceBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class InvoiceActivity: AppCompatActivity(), ProductAdapter.OnItemClickListenerProduct, ProductAdapter.OnLongItemClickListenerProduct {
	//Биндинг
	private lateinit var binding: ActivityInvoiceBinding
	private lateinit var listArray: ArrayList<ProductItem>
	private lateinit var dialog: Dialog
	private lateinit var dialogCount: Dialog
	
	companion object {
		fun hideKeyboard(view: View) {
			val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			imm.hideSoftInputFromWindow(view.windowToken, 0)
			view.clearFocus()
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		sharedPreferences = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
		MainActivity.setTheme(this) //Создание самого проекта и привязки!
		super.onCreate(savedInstanceState)
		binding = ActivityInvoiceBinding.inflate(layoutInflater)
		setContentView(binding.root)
		listArray = ArrayList()
		for(i in 0 .. 10) {
			listArray.add(ProductItem(i.toLong(), getString(R.string.product_name_product_item_txt_hint) + " $i", 1000,getString(R.string.product_description_product_item_txt_hint) + " $i", null, 100.00))
		}
		binding.addInvoiceProduct.setOnClickListener {
			dialog = Dialog(this)
			dialog.setCancelable(true)
			dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog)
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
			dialog.setContentView(R.layout.select_product_to_invoice)
			val productsList = dialog.findViewById<RecyclerView>(R.id.ownProductsList)
			productsList.layoutManager = LinearLayoutManager(this)
			productsList.adapter = ProductAdapter(listArray, this, this)
			val productSearch = dialog.findViewById<TextInputEditText>(R.id.ownProductName)
			val close = dialog.findViewById<ImageButton>(R.id.closeAddProductToInvoice)
			val header = dialog.findViewById<TextView>(R.id.addEditProductToInvoiceHeaderTxt)
			header.text = getString(R.string.add_product)
			productSearch.doAfterTextChanged {
				val newList = listArray.filter { it.name.lowercase().startsWith(productSearch.text.toString().lowercase()) }
				productsList.adapter = ProductAdapter(newList as ArrayList<ProductItem>, this, this)
			}
			productSearch.setOnEditorActionListener { _, actionId, _ ->
				if(actionId == EditorInfo.IME_ACTION_SEARCH) {
					hideKeyboard(productSearch)
					true
				}
				else false
			}
			close.setOnClickListener {
				dialog.dismiss()
			}
			dialog.show()
			productSearch.requestFocus()
			Handler(Looper.getMainLooper()).postDelayed({
				val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
				imm.showSoftInput(productSearch, InputMethodManager.SHOW_IMPLICIT)
			}, 100) // Задержка в 100 миллисекунд
		}
		binding.backToAllInvoices.setOnClickListener {
			finish()
		}
	}
	
	override fun onItemClickProduct(position: Int) {
		dialog.dismiss()
		dialogCount = Dialog(this)
		dialogCount.setCancelable(true)
		dialogCount.window!!.setBackgroundDrawableResource(R.drawable.dialog)
		dialogCount.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialogCount.setContentView(R.layout.select_product_count_to_invoice)
		val productCount = dialogCount.findViewById<TextInputEditText>(R.id.ownProductCount)
		val productCountTxt = dialogCount.findViewById<TextInputLayout>(R.id.countOwnProductsTxt)
		val close = dialogCount.findViewById<ImageButton>(R.id.closeAddCountProductToInvoice)
		val header = dialogCount.findViewById<TextView>(R.id.addEditCountToInvoiceHeaderTxt)
		header.text = getString(R.string.add_product)
		productCount.doAfterTextChanged {
			if(it!!.isNotEmpty()) {
				productCountTxt.error = null
			}
		}
		productCount.setOnEditorActionListener { _, actionId, _ ->
			if(actionId == EditorInfo.IME_ACTION_DONE) {
				if(productCount.text!!.isEmpty()){
					productCountTxt.error = getString(R.string.error_input)
					Handler(Looper.getMainLooper()).postDelayed({
						val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
						imm.showSoftInput(productCount, InputMethodManager.SHOW_IMPLICIT)
					}, 100) // Задержка в 100 миллисекунд
					false
				}
				else {
					hideKeyboard(productCount)
					dialogCount.dismiss()
					true
				}
			}
			else false
		}
		close.setOnClickListener {
			dialogCount.dismiss()
		}
		dialogCount.show()
		productCount.requestFocus()
		Handler(Looper.getMainLooper()).postDelayed({
			val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			imm.showSoftInput(productCount, InputMethodManager.SHOW_IMPLICIT)
		}, 100) // Задержка в 100 миллисекунд
	}
	
	override fun onLongItemClickProduct(position: Int) {
	}
}
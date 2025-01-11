package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfadev.bizlinker.AddEditProductActivity.Companion.editProductItem
import com.alfadev.bizlinker.InvoiceActivity.Companion.hideKeyboard
import com.alfadev.bizlinker.LoginActivity.Companion.organization
import com.alfadev.bizlinker.MainActivity.Companion.PRODUCTS
import com.alfadev.bizlinker.MainActivity.Companion.BASE_URL
import com.alfadev.bizlinker.MainActivity.Companion.GET_ORGANIZATION
import com.alfadev.bizlinker.MainActivity.Companion.GET_PRODUCTS
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_TXT
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_TXT
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE_START
import com.alfadev.bizlinker.MainActivity.Companion.WISHLIST
import com.alfadev.bizlinker.MainActivity.Companion.client
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.databinding.ProductsFragmentBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ProductsFragment : Fragment(), ProductAdapter.OnItemClickListenerProduct,
	ProductAdapter.OnLongItemClickListenerProduct, WishlistAdapter.OnItemClickListenerWishlist,
	WishlistAdapter.OnLongItemClickListenerWishlist {
	//Биндинг
	private lateinit var binding: ProductsFragmentBinding
	private lateinit var listProducts: ArrayList<ProductItem>
	private lateinit var listWishlist: ArrayList<WishlistItem>
	private lateinit var dialog: Dialog
	private var page = 1
	
	@SuppressLint("UseCompatLoadingForDrawables")
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = ProductsFragmentBinding.inflate(inflater, container, false)
		binding.products.layoutManager = LinearLayoutManager(this.context)
		binding.searchProducts.setOnClickListener {
			if (binding.productsHeaderTxt.visibility == VISIBLE) {
				binding.productsHeaderTxt.visibility = INVISIBLE
				binding.searchProductsET.visibility = VISIBLE
				binding.searchProductsET.requestFocus()
				val imm =
					activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
				imm?.showSoftInput(binding.searchProductsET, InputMethodManager.SHOW_IMPLICIT)
			} else {
				if (binding.searchProductsET.isFocused) {
					hideKeyboard(binding.searchProductsET)
				} else {
					binding.searchProductsET.requestFocus()
					val imm =
						activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
					imm?.showSoftInput(binding.searchProductsET, InputMethodManager.SHOW_IMPLICIT)
				}
				if (binding.searchProductsET.text.isNullOrEmpty()) {
					binding.productsHeaderTxt.visibility = VISIBLE
					binding.searchProductsET.visibility = INVISIBLE
				}
			}
		}
		binding.searchProductsET.setOnEditorActionListener { _, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				hideKeyboard(binding.searchProductsET)
				if (binding.searchProductsET.text.isNullOrEmpty()) {
					binding.productsHeaderTxt.visibility = VISIBLE
					binding.searchProductsET.visibility = INVISIBLE
				}
				true
			} else {
				false
			}
		}
		binding.searchProductsET.doAfterTextChanged {
			if (sharedPreferences.getString("role", "provider") == "provider") {
				val newList = listProducts.filter {
					it.name.lowercase()
						.contains(binding.searchProductsET.text.toString().lowercase())
				}
				binding.products.adapter =
					ProductAdapter(newList as ArrayList<ProductItem>, this, this)
			} else {
				val newList = listWishlist.filter {
					it.name.lowercase()
						.contains(binding.searchProductsET.text.toString().lowercase())
				}
				binding.products.adapter =
					WishlistAdapter(newList as ArrayList<WishlistItem>, this, this)
			}
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
			if (it!!.isNotEmpty()) {
				nameTxt.error = null
			}
		}
		count.doAfterTextChanged {
			if (it!!.isNotEmpty()) {
				countTxt.error = null
			}
		}
		price.doAfterTextChanged {
			if (it!!.isNotEmpty()) {
				priceTxt.error = null
			}
		}
		val close = dialog.findViewById<ImageButton>(R.id.backToWishlistFromAddEditWishlist)
		val save = dialog.findViewById<Button>(R.id.buttonAddEditWishlistSave)
		save.setOnClickListener {
			if (name.text!!.isEmpty()) {
				nameTxt.error = getString(R.string.error_input)
			}
			if (count.text!!.isEmpty()) {
				countTxt.error = getString(R.string.error_input)
			}
			if (price.text!!.isEmpty()) {
				priceTxt.error = getString(R.string.error_input)
			}
			if (name.text!!.isNotEmpty() && count.text!!.isNotEmpty() && price.text!!.isNotEmpty()) {
				val requestBody = FormBody.Builder()
					.add("name", name.text.toString())
					.add("count", count.text.toString())
					.add("price", price.text.toString()).build()
				val request = if (editItemWishlist != null) {
					Request.Builder()
						.url("$BASE_URL$WISHLIST/${editItemWishlist!!.id}")
						.addHeader(HEADER_TXT, HEADER_VALUE)
						.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
						.put(requestBody)
						.build()
				} else {
					Request.Builder()
						.url("$BASE_URL$WISHLIST")
						.addHeader(HEADER_TXT, HEADER_VALUE)
						.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
						.post(requestBody)
						.build()
				}
				client.newCall(request).enqueue(object : Callback {
					override fun onResponse(call: Call, response: Response) {
						if (response.isSuccessful) {
							response.use {
								val responseBody = it.body?.string() // Читаем тело ответа
								val json = JSONObject(responseBody!!)
								updateData()
								Log.e("RESPONSE", json.toString())
							}
						} else {
							response.use {
								val responseBody = it.body?.string() // Читаем тело ответа
								val json = JSONObject(responseBody!!)
								Log.e("RESPONSE", json.toString())
							}
						}
					}
					
					override fun onFailure(call: Call, e: IOException) {
						Log.e("RESPONSE", e.toString())
					}
				})
				close.performClick()
			}
		}
		close.setOnClickListener {
			editItemWishlist = null
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
			if (sharedPreferences.getString("role", "provider") == "provider") {
				val intent = Intent(context, AddEditProductActivity::class.java)
				resultLauncher.launch(intent)
			} else {
				header.text = getString(R.string.add_product)
				editItemWishlist = null
				dialog.show()
			}
		}
		updateData()
		return binding.root
	}
	
	private fun updateData() {
		if (sharedPreferences.getString("role", "provider") == "provider") {
			listProducts = ArrayList()
			val request = Request.Builder()
				.url("$BASE_URL$GET_ORGANIZATION${organization.id}$GET_PRODUCTS?page=$page")
				.addHeader(HEADER_TXT, HEADER_VALUE)
				.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
				.get()
				.build()
			client.newCall(request).enqueue(object : Callback {
				override fun onResponse(call: Call, response: Response) {
					if (response.isSuccessful) {
						response.use {
							val responseBody = it.body?.string() // Читаем тело ответа
							val json = JSONObject(responseBody!!)
							val array = json.getJSONArray("data")
							val gson = Gson()
							for (i in 0..<array.length()) {
								val jsonObject = array.getJSONObject(i)
								val product =
									gson.fromJson(jsonObject.toString(), ProductItem::class.java)
								Log.e("Gson", product.toString())
								listProducts.add(product)
							}
							this@ProductsFragment.requireActivity().runOnUiThread {
								binding.products.adapter =
									ProductAdapter(
										listProducts,
										this@ProductsFragment,
										this@ProductsFragment
									)
							}
							Log.e("RESPONSE", json.toString())
						}
					} else {
						Log.e("RESPONSE", response.toString())
					}
				}
				
				override fun onFailure(call: Call, e: IOException) {
					Log.e("RESPONSE", e.toString())
				}
			})
		} else {
			listWishlist = ArrayList()
			val request = Request.Builder()
				.url("$BASE_URL$WISHLIST")
				.addHeader(HEADER_TXT, HEADER_VALUE)
				.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
				.get()
				.build()
			client.newCall(request).enqueue(object : Callback {
				override fun onResponse(call: Call, response: Response) {
					if (response.isSuccessful) {
						response.use {
							val responseBody = it.body?.string() // Читаем тело ответа
							val array = JSONArray(responseBody!!)
							val gson = Gson()
							for (i in 0..<array.length()) {
								val jsonObject = array.getJSONObject(i)
								val wishlistItem =
									gson.fromJson(jsonObject.toString(), WishlistItem::class.java)
								Log.e("Gson", wishlistItem.toString())
								wishlistItem.organization = organization
								listWishlist.add(wishlistItem)
							}
							this@ProductsFragment.requireActivity().runOnUiThread {
								binding.products.adapter =
									WishlistAdapter(
										listWishlist,
										this@ProductsFragment,
										this@ProductsFragment
									)
							}
							Log.e("RESPONSE", array.toString())
						}
					} else {
						Log.e("RESPONSE", response.toString())
					}
				}
				
				override fun onFailure(call: Call, e: IOException) {
					Log.e("RESPONSE", e.toString())
				}
			})
			
		}
	}
	
	private val resultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == AppCompatActivity.RESULT_OK) {
				updateData()
			}
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
		val intent = Intent(context, AddEditProductActivity::class.java)
		resultLauncher.launch(intent)
	}
	
	override fun onLongItemClickProduct(position: Int) {
		val deleteProduct = listProducts[position]
		val dlgAlert = AlertDialog.Builder(
			this@ProductsFragment.requireActivity(),
			R.style.MyDialogThemeException
		)
		dlgAlert.setTitle("Подтвердите удаление!")
		dlgAlert.setCancelable(true)
		dlgAlert.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
			val request = Request.Builder()
				.url("$BASE_URL$PRODUCTS/${deleteProduct.id}")
				.addHeader(HEADER_TXT, HEADER_VALUE)
				.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
				.delete()
				.build()
			client.newCall(request).enqueue(object : Callback {
				override fun onResponse(call: Call, response: Response) {
					if (response.isSuccessful) {
						response.use {
							listProducts.remove(deleteProduct)
							this@ProductsFragment.requireActivity().runOnUiThread {
								binding.products.adapter =
									ProductAdapter(
										listProducts,
										this@ProductsFragment,
										this@ProductsFragment
									)
							}
						}
					} else {
						response.use {
							val responseBody = it.body?.string() // Читаем тело ответа
							val json = JSONObject(responseBody!!)
							Log.e("RESPONSE", json.toString())
						}
					}
				}
				
				override fun onFailure(call: Call, e: IOException) {
					Log.e("RESPONSE", e.toString())
				}
			})
		}
		dlgAlert.setNegativeButton("Нет") { _: DialogInterface?, _: Int -> }
		val connDialog = dlgAlert.create()
		connDialog.setOnShowListener {
			val p = connDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			p.setTextColor(
				resources.getColor(
					R.color.whiteDark,
					this@ProductsFragment.requireActivity().theme
				)
			)
			val n = connDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
			n.setTextColor(
				resources.getColor(
					R.color.whiteDark,
					this@ProductsFragment.requireActivity().theme
				)
			)
		}
		connDialog.show()
	}
	
	private var editItemWishlist: WishlistItem? = null
	
	@SuppressLint("SetTextI18n")
	override fun onItemClickWishlist(position: Int) {
		editItemWishlist = listWishlist[position]
		val header = dialog.findViewById<TextView>(R.id.addEditWishlistHeaderTxt)
		val name = dialog.findViewById<TextInputEditText>(R.id.addEditWishlistNameTIET)
		val count = dialog.findViewById<TextInputEditText>(R.id.addEditWishlistCountTIET)
		val price = dialog.findViewById<TextInputEditText>(R.id.addEditWishlistPriceTIET)
		name.setText(editItemWishlist!!.name)
		count.setText(editItemWishlist!!.count.toString())
		price.setText(editItemWishlist!!.price.toString())
		header.text = getString(R.string.edit_product)
		dialog.show()
	}
	
	override fun onLongItemClickWishlist(position: Int) {
		val deleteWishlist = listWishlist[position]
		val dlgAlert = AlertDialog.Builder(
			this@ProductsFragment.requireActivity(),
			R.style.MyDialogThemeException
		)
		dlgAlert.setTitle("Подтвердите удаление!")
		dlgAlert.setCancelable(true)
		dlgAlert.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
			val request = Request.Builder()
				.url("$BASE_URL$WISHLIST/${deleteWishlist.id}")
				.addHeader(HEADER_TXT, HEADER_VALUE)
				.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
				.delete()
				.build()
			client.newCall(request).enqueue(object : Callback {
				override fun onResponse(call: Call, response: Response) {
					if (response.isSuccessful) {
						response.use {
							listWishlist.remove(deleteWishlist)
							this@ProductsFragment.requireActivity().runOnUiThread {
								binding.products.adapter =
									WishlistAdapter(
										listWishlist,
										this@ProductsFragment,
										this@ProductsFragment
									)
							}
						}
					} else {
						response.use {
							val responseBody = it.body?.string() // Читаем тело ответа
							val json = JSONObject(responseBody!!)
							Log.e("RESPONSE", json.toString())
						}
					}
				}
				
				override fun onFailure(call: Call, e: IOException) {
					Log.e("RESPONSE", e.toString())
				}
			})
		}
		dlgAlert.setNegativeButton("Нет") { _: DialogInterface?, _: Int -> }
		val connDialog = dlgAlert.create()
		connDialog.setOnShowListener {
			val p = connDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			p.setTextColor(
				resources.getColor(
					R.color.whiteDark,
					this@ProductsFragment.requireActivity().theme
				)
			)
			val n = connDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
			n.setTextColor(
				resources.getColor(
					R.color.whiteDark,
					this@ProductsFragment.requireActivity().theme
				)
			)
		}
		connDialog.show()
	}
}

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
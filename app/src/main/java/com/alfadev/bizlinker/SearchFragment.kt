package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfadev.bizlinker.InvoiceActivity.Companion.hideKeyboard
import com.alfadev.bizlinker.MainActivity.Companion.BASE_URL
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_TXT
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.ORGANIZATIONS
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_TXT
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE_START
import com.alfadev.bizlinker.MainActivity.Companion.WISHLIST
import com.alfadev.bizlinker.MainActivity.Companion.client
import com.alfadev.bizlinker.databinding.SearchFragmentBinding
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment: Fragment() {
	private var param1: String? = null
	private var param2: String? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			param1 = it.getString(ARG_PARAM1)
			param2 = it.getString(ARG_PARAM2)
		}
	}
	//Биндинг
	private var page = 1
	private lateinit var binding: SearchFragmentBinding
	private lateinit var listOrganizations: ArrayList<OrganizationItem>
	@SuppressLint("UseCompatLoadingForDrawables")
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = SearchFragmentBinding.inflate(inflater, container, false)
		binding.searchSearch.setOnClickListener {
			if(binding.searchHeaderTxt.visibility == VISIBLE) {
				binding.searchHeaderTxt.visibility = INVISIBLE
				binding.searchOrganizationET.visibility = VISIBLE
				binding.searchOrganizationET.requestFocus()
				val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
				imm?.showSoftInput(binding.searchOrganizationET, InputMethodManager.SHOW_IMPLICIT)
			}
			else {
				if(binding.searchOrganizationET.isFocused) {
					hideKeyboard(binding.searchOrganizationET)
				}
				else {
					binding.searchOrganizationET.requestFocus()
					val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
					imm?.showSoftInput(binding.searchOrganizationET, InputMethodManager.SHOW_IMPLICIT)
				}
				if(binding.searchOrganizationET.text.isNullOrEmpty()) {
					binding.searchHeaderTxt.visibility = VISIBLE
					binding.searchOrganizationET.visibility = INVISIBLE
				}
			}
		}
		binding.searchOrganizationET.setOnEditorActionListener { _, actionId, _ ->
			if(actionId == EditorInfo.IME_ACTION_SEARCH) {
				hideKeyboard(binding.searchOrganizationET)
				if(binding.searchOrganizationET.text.isNullOrEmpty()) {
					binding.searchHeaderTxt.visibility = VISIBLE
					binding.searchOrganizationET.visibility = INVISIBLE
				}
				true
			}
			else {
				false
			}
		}
		binding.searchOrganizationET.doAfterTextChanged {
			val newList = listOrganizations.filter { it.name.lowercase().contains(binding.searchOrganizationET.text.toString().lowercase()) }
			binding.search.adapter = SearchAdapter(this@SearchFragment.requireActivity(),newList as ArrayList<OrganizationItem>)
		}
		listOrganizations = ArrayList()
		binding.search.layoutManager = LinearLayoutManager(this.context)
		val request = Request.Builder()
			.url("$BASE_URL$ORGANIZATIONS/?page=$page")
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
							val organization =
								gson.fromJson(jsonObject.toString(), OrganizationItem::class.java)
							Log.e("Gson", organization.toString())
							if (organization.id != LoginActivity.organization.id) {
								listOrganizations.add(organization)
							}
						}
						this@SearchFragment.requireActivity().runOnUiThread {
							binding.search.adapter = SearchAdapter(this@SearchFragment.requireActivity(),listOrganizations)
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
		binding.filterSearch.setOnClickListener {
			getFilters()
		}
		return binding.root
	}
	
	private val hFilters = Handler(Looper.getMainLooper())
	private val rFilters = Runnable { getFilters() }
	private fun getFilters() {
		binding.cardviewFiltersOther.pivotY = 0f
		if(binding.cardviewFiltersOther.scaleY == 0f) {
			binding.cardviewFiltersOther.visibility = VISIBLE
			binding.cardviewFiltersOther.animate().scaleY(1f).setDuration(500).start()
			hFilters.removeCallbacks(rFilters)
			hFilters.postDelayed(rFilters, 30000)
		}
		else {
			hFilters.removeCallbacks(rFilters)
			binding.cardviewFiltersOther.animate().scaleY(0f).withEndAction {
				binding.cardviewFiltersOther.visibility = INVISIBLE
			}.setDuration(500).start()
		}
	}
}
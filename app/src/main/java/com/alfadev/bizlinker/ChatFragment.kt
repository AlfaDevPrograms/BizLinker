package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfadev.bizlinker.InvoiceActivity.Companion.hideKeyboard
import com.alfadev.bizlinker.LoginActivity.Companion.organization
import com.alfadev.bizlinker.MainActivity.Companion.BASE_URL
import com.alfadev.bizlinker.MainActivity.Companion.CHATS
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_TXT
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.PRODUCTS
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_TXT
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE_START
import com.alfadev.bizlinker.MainActivity.Companion.channel
import com.alfadev.bizlinker.MainActivity.Companion.client
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.SelectedChatActivity.Companion.selectedChat
import com.alfadev.bizlinker.databinding.ChatFragmentBinding
import com.google.gson.Gson
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatFragment : Fragment(), ChatAdapter.OnItemClickListenerChat,
	ChatAdapter.OnLongItemClickListenerChat,
	MainActivity.OnMyEventListener {
	companion object {
		lateinit var onMyEventListener: MainActivity.OnMyEventListener
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
	
	//Биндинг
	private lateinit var binding: ChatFragmentBinding
	private lateinit var listArray: ArrayList<ChatItem>
	
	@SuppressLint("UseCompatLoadingForDrawables")
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = ChatFragmentBinding.inflate(inflater, container, false)
		onMyEventListener = this
		binding.searchUsers.setOnClickListener {
			if (binding.chatHeaderTxt.visibility == VISIBLE) {
				binding.chatHeaderTxt.visibility = INVISIBLE
				binding.searchChatET.visibility = VISIBLE
				binding.searchChatET.requestFocus()
				val imm =
					activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
				imm?.showSoftInput(binding.searchChatET, InputMethodManager.SHOW_IMPLICIT)
			} else {
				if (binding.searchChatET.isFocused) {
					hideKeyboard(binding.searchChatET)
				} else {
					binding.searchChatET.requestFocus()
					val imm =
						activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
					imm?.showSoftInput(binding.searchChatET, InputMethodManager.SHOW_IMPLICIT)
				}
				if (binding.searchChatET.text.isNullOrEmpty()) {
					binding.chatHeaderTxt.visibility = VISIBLE
					binding.searchChatET.visibility = INVISIBLE
				}
			}
		}
		binding.searchChatET.setOnEditorActionListener { _, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				hideKeyboard(binding.searchChatET)
				if (binding.searchChatET.text.isNullOrEmpty()) {
					binding.chatHeaderTxt.visibility = VISIBLE
					binding.searchChatET.visibility = INVISIBLE
				}
				true
			} else {
				false
			}
		}
		binding.searchChatET.doAfterTextChanged {
			val newList = listArray.filter {
				it.receiver.name.lowercase()
					.contains(binding.searchChatET.text.toString().lowercase())
			}
			binding.chats.adapter = ChatAdapter(newList as ArrayList<ChatItem>, this, this)
		}
		if (sharedPreferences.getString("role", "provider") == "provider") {
			binding.buttonAllInvoices.visibility = VISIBLE
		} else {
			binding.buttonAllInvoices.visibility = INVISIBLE
		}
		binding.chats.layoutManager = LinearLayoutManager(this.context)
		getChats()
		binding.buttonAllInvoices.setOnClickListener {
			val myIntent = Intent(this@ChatFragment.context, AllInvoicesActivity::class.java)
			startActivity(myIntent)
		}
		return binding.root
	}
	
	private fun getChats() {
		listArray = ArrayList()
		val request = Request.Builder()
			.url("$BASE_URL$CHATS")
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
							val chatItem =
								gson.fromJson(jsonObject.toString(), ChatItem::class.java)
							Log.e("Gson", chatItem.toString())
							listArray.add(chatItem)
						}
						this@ChatFragment.requireActivity().runOnUiThread {
							binding.chats.adapter = ChatAdapter(listArray, this@ChatFragment, this@ChatFragment)
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
	
	override fun onItemClickChat(position: Int) {
		val chat = listArray[position]
		selectedChat = chat
		val intent = Intent(context, SelectedChatActivity::class.java)
		resultLauncher.launch(intent)
	}
	
	override fun onLongItemClickChat(position: Int) {
		val deleteChat = listArray[position]
		val dlgAlert = AlertDialog.Builder(
			this@ChatFragment.requireActivity(),
			R.style.MyDialogThemeException
		)
		dlgAlert.setTitle("Подтвердите удаление!")
		dlgAlert.setMessage("Чат будет удален у обоих собеседников!")
		dlgAlert.setCancelable(true)
		dlgAlert.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
			val request = Request.Builder()
				.url("$BASE_URL$CHATS/${deleteChat.id}")
				.addHeader(HEADER_TXT, HEADER_VALUE)
				.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
				.delete()
				.build()
			client.newCall(request).enqueue(object : Callback {
				override fun onResponse(call: Call, response: Response) {
					if (response.isSuccessful) {
						response.use {
							listArray.remove(deleteChat)
							this@ChatFragment.requireActivity().runOnUiThread {
								binding.chats.adapter = ChatAdapter(listArray, this@ChatFragment, this@ChatFragment)
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
					this@ChatFragment.requireActivity().theme
				)
			)
			val n = connDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
			n.setTextColor(
				resources.getColor(
					R.color.whiteDark,
					this@ChatFragment.requireActivity().theme
				)
			)
		}
		connDialog.show()
	}
	private val resultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == AppCompatActivity.RESULT_OK) {
				getChats()
			}
		}
	override fun onEventOccurred(message: String) {
		channel.bind("App\\Events\\ChatMessageSent", object :
			PrivateChannelEventListener {
			override fun onEvent(event: PusherEvent?) {
				getChats()
			}
			
			override fun onSubscriptionSucceeded(channelName: String?) {
				Log.e("НАХУЙ", "ИДИ НАХУЙ1")
			}
			
			override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {
				Log.e("НАХУЙ", "ИДИ НАХУЙ2")
			}
		})
	}
}
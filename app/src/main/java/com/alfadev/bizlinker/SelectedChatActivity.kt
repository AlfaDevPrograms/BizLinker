package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfadev.bizlinker.LoginActivity.Companion.organization
import com.alfadev.bizlinker.MainActivity.Companion.BASE_URL
import com.alfadev.bizlinker.MainActivity.Companion.CHATS
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_TXT
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.ORGANIZATIONS
import com.alfadev.bizlinker.MainActivity.Companion.PIN_MESSAGE
import com.alfadev.bizlinker.MainActivity.Companion.SEND_MESSAGE
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_TXT
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE_START
import com.alfadev.bizlinker.MainActivity.Companion.UNPIN_MESSAGE
import com.alfadev.bizlinker.MainActivity.Companion.channel
import com.alfadev.bizlinker.MainActivity.Companion.client
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.databinding.ActivitySelectedChatBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SelectedChatActivity: AppCompatActivity(), MessagesAdapter.OnItemClickListener {
	companion object {
		lateinit var selectedChat: ChatItem
		lateinit var listMessages: ArrayList<MessageItem>
	}
	//Биндинг
	private  lateinit var adapter: MessagesAdapter
	private lateinit var binding: ActivitySelectedChatBinding
	private lateinit var currentMessage: MessageItem
	private lateinit var pinTxt:TextView
	private lateinit var popupView: View
	private lateinit var popupWindow:PopupWindow
	private fun showPopup() {
		popupWindow.showAtLocation(binding.cardviewInput, Gravity.BOTTOM, 0, 0) // Показываем снизу экрана
	}
	private val resultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				getMessages()
			}
		}
	@SuppressLint("InflateParams")
	override fun onCreate(savedInstanceState: Bundle?) {
		sharedPreferences = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
		MainActivity.setTheme(this) //Создание самого проекта и привязки!
		super.onCreate(savedInstanceState)
		binding = ActivitySelectedChatBinding.inflate(layoutInflater)
		setContentView(binding.root)
		popupView = LayoutInflater.from(this).inflate(R.layout.message_menu, null)
		val pin = popupView.findViewById<LinearLayout>(R.id.pinMessageItem)
		pinTxt = popupView.findViewById(R.id.pinMessageItemTxt)
		val edit = popupView.findViewById<LinearLayout>(R.id.editMessageItem)
		val delete = popupView.findViewById<LinearLayout>(R.id.deleteMessageItem)
		popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
		popupWindow.animationStyle = R.style.PopupAnimation
		pin.setOnClickListener {
			runOnUiThread {
				popupWindow.dismiss()
			}
			val requestBody = FormBody.Builder()
				.add("message", currentMessage.id.toString()).build()
			val url = if (currentMessage.pinned) {
				UNPIN_MESSAGE
			} else {
				PIN_MESSAGE
			}
			val request = Request.Builder()
				.url("$BASE_URL$CHATS/${selectedChat.id}$url")
				.addHeader(HEADER_TXT, HEADER_VALUE)
				.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
				.post(requestBody)
				.build()
			client.newCall(request).enqueue(object : Callback {
				override fun onResponse(call: Call, response: Response) {
					if (response.isSuccessful) {
						response.use {
							listMessages[listMessages.indexOf(currentMessage)].pinned =
								!currentMessage.pinned
							listMessages.sortBy { it.date }
							adapter = MessagesAdapter(listMessages, this@SelectedChatActivity)
							runOnUiThread {
								checkPin()
								binding.allMesagesChat.adapter = adapter
								binding.allMesagesChat.scrollToPosition(adapter.itemCount - 1)
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
		// Делаем так, чтобы PopupWindow отображался внизу
		popupWindow.isFocusable = true
		if(sharedPreferences.getString("role", "provider") == "provider") {
			binding.addInvoice.visibility = VISIBLE
		}
		else {
			binding.addInvoice.visibility = INVISIBLE
		}
		binding.pinChatHeader.setOnClickListener {
			val intent = Intent(this, PinnedMessagesActivity::class.java)
			resultLauncher.launch(intent)
		}
		binding.selectedChatUsernameTxt.text = selectedChat.receiver.name
		var correctName = selectedChat.receiver.name
		correctName = correctName.replace("\"","")
		correctName = correctName.replace(" ", "")
		correctName = try {
			correctName.substring(3, 6)
		} catch(ex:Exception){
			correctName.substring(3,correctName.length)
		}
		binding.selectedChatAvaTxt.text = correctName
		binding.backToChats.setOnClickListener {
			returnData()
		}
		binding.selectedChatMessage.doAfterTextChanged {
			if(it.isNullOrEmpty()) {
				if(binding.attach.visibility == INVISIBLE) {
					binding.attach.visibility = VISIBLE
					binding.send.visibility = INVISIBLE
				}
			}
			else {
				if(binding.send.visibility == INVISIBLE) {
					binding.send.visibility = VISIBLE
					binding.attach.visibility = INVISIBLE
				}
			}
		}
		binding.allMesagesChat.layoutManager = LinearLayoutManager(this)
		getMessages()
		binding.selectedChatMessage.setOnFocusChangeListener { view, hasFocus ->
			if (hasFocus) {
				adapter = MessagesAdapter(listMessages, this@SelectedChatActivity)
				binding.allMesagesChat.adapter = adapter
				binding.allMesagesChat.scrollToPosition(adapter.itemCount - 1)
			} else {
			}
		}
		channel.bind("App\\Events\\ChatMessageSent", object :
			PrivateChannelEventListener {
			override fun onEvent(event: PusherEvent?) {
				val json = JSONObject(event!!.data)
				val gson = Gson()
				val messageItem = gson.fromJson(json.toString(), EventReceiverItem::class.java)
				if (selectedChat.id == messageItem.chat_id && messageItem.sender.id != organization.id) {
					listMessages.add(messageItem.message)
					listMessages.sortBy { it.date }
					adapter = MessagesAdapter(listMessages, this@SelectedChatActivity)
					runOnUiThread {
						binding.allMesagesChat.adapter = adapter
						binding.allMesagesChat.scrollToPosition(adapter.itemCount - 1)
					}
				}
			}
			
			override fun onSubscriptionSucceeded(channelName: String?) {
				Log.e("НАХУЙ", "ИДИ НАХУЙ1")
			}
			
			override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {
				Log.e("НАХУЙ", "ИДИ НАХУЙ2")
			}
		})
		binding.send.setOnClickListener {
			var message = binding.selectedChatMessage.text.toString()
			message = message.replace(Regex("^[\n\r]+|[\n\r]+$"), "").trim()
			val requestBody = FormBody.Builder()
				.add("message", message).build()
			val request = Request.Builder()
				.url("$BASE_URL$CHATS/${selectedChat.id}$SEND_MESSAGE")
				.addHeader(HEADER_TXT, HEADER_VALUE)
				.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
				.post(requestBody)
				.build()
			client.newCall(request).enqueue(object : Callback {
				override fun onResponse(call: Call, response: Response) {
					if (response.isSuccessful) {
						response.use {
							val responseBody = it.body?.string() // Читаем тело ответа
							val json = JSONObject(responseBody!!)
							val gson = Gson()
							val messageItem = gson.fromJson(json.toString(), MessageItem::class.java)
							listMessages.add(messageItem)
							listMessages.sortBy { it.date }
							adapter = MessagesAdapter(listMessages, this@SelectedChatActivity)
							runOnUiThread {
								binding.allMesagesChat.adapter = adapter
								binding.allMesagesChat.scrollToPosition(adapter.itemCount - 1)
							}
							Log.e("RESPONSE", json.toString())
						}
					} else {
						response.use {
							val responseBody = it.body?.string() // Читаем тело ответа
							val json = JSONObject(responseBody!!)
							Log.e("RESPONSE", json.toString())
						}
					}
					Log.e("mes", message)
					runOnUiThread {
						binding.selectedChatMessage.text.clear()
						binding.attach.visibility = VISIBLE
						binding.send.visibility = INVISIBLE
					}
				}
				
				override fun onFailure(call: Call, e: IOException) {
					Log.e("mes", message)
					runOnUiThread {
						binding.selectedChatMessage.text.clear()
						binding.attach.visibility = VISIBLE
						binding.send.visibility = INVISIBLE
					}
					Log.e("RESPONSE", e.toString())
				}
			})
		}
	}
	private fun returnData() {
		val intent = Intent()
		setResult(RESULT_OK, intent)
		finish() // Закрываем активность
	}
	private fun getMessages(){
		listMessages = ArrayList()
		val request = Request.Builder()
			.url("$BASE_URL$CHATS/${selectedChat.id}")
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
						val gson = Gson()
						val chatData = gson.fromJson(json.toString(), ChatDataItem::class.java)
						Log.e("RESPONSE_CHATDATA", chatData.toString())
						listMessages.addAll(chatData.messages)
						listMessages.sortBy { it.date }
						runOnUiThread {
							adapter = MessagesAdapter(listMessages, this@SelectedChatActivity)
							binding.allMesagesChat.adapter = adapter
							binding.allMesagesChat.scrollToPosition(adapter.itemCount - 1)
							checkPin()
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
	private fun checkPin()
	{
		val list = listMessages.filter { it.pinned }
		if (list.isNotEmpty()) {
			binding.pinChatHeader.visibility = VISIBLE
			binding.lastPinTxt.text = list[list.lastIndex].message
		} else {
			binding.pinChatHeader.visibility = GONE
		}
	}
	private fun hideKeyboard(activity: Activity) {
		val view = activity.currentFocus
		view?.let {
			val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
		}
	}
	override fun onItemClick(position: Int) {
		currentMessage = listMessages[position]
		if (currentMessage.pinned) {
			pinTxt.text = getString(R.string.unpin)
		} else {
			pinTxt.text = getString(R.string.pin)
		}
		hideKeyboard(this)
		showPopup()
	}
}

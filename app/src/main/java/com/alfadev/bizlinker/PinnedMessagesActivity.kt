package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfadev.bizlinker.LoginActivity.Companion.organization
import com.alfadev.bizlinker.MainActivity.Companion.BASE_URL
import com.alfadev.bizlinker.MainActivity.Companion.CHATS
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_TXT
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.PIN_MESSAGE
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_TXT
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE_START
import com.alfadev.bizlinker.MainActivity.Companion.UNPIN_MESSAGE
import com.alfadev.bizlinker.MainActivity.Companion.channel
import com.alfadev.bizlinker.MainActivity.Companion.client
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.SelectedChatActivity.Companion.listMessages
import com.alfadev.bizlinker.SelectedChatActivity.Companion.selectedChat
import com.alfadev.bizlinker.databinding.PinnedMessagesBinding
import com.google.gson.Gson
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PinnedMessagesActivity: AppCompatActivity(), MessagesAdapter.OnItemClickListener {
	//Биндинг
	private lateinit var listPinnedMessages: ArrayList<MessageItem>
	private  lateinit var adapter: MessagesAdapter
	private lateinit var binding: PinnedMessagesBinding
	private lateinit var currentMessage: MessageItem
	private lateinit var pinTxt:TextView
	private lateinit var popupView: View
	private lateinit var popupWindow:PopupWindow
	private fun showPopup() {
		popupWindow.showAtLocation(binding.allPinnedMesagesChat, Gravity.BOTTOM, 0, 0) // Показываем снизу экрана
	}
	
	@SuppressLint("InflateParams")
	override fun onCreate(savedInstanceState: Bundle?) {
		sharedPreferences = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
		MainActivity.setTheme(this) //Создание самого проекта и привязки!
		super.onCreate(savedInstanceState)
		binding = PinnedMessagesBinding.inflate(layoutInflater)
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
							listPinnedMessages[listPinnedMessages.indexOf(currentMessage)].pinned =
								!currentMessage.pinned
							listPinnedMessages.sortBy { it.date }
							adapter = MessagesAdapter(listPinnedMessages.filter { it.pinned } as ArrayList<MessageItem>, this@PinnedMessagesActivity)
							runOnUiThread {
								checkPin()
								binding.allPinnedMesagesChat.adapter = adapter
								binding.allPinnedMesagesChat.scrollToPosition(adapter.itemCount - 1)
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
		binding.backToMessages.setOnClickListener {
			returnData()
		}
		binding.allPinnedMesagesChat.layoutManager = LinearLayoutManager(this)
		getMessages()
		channel.bind("App\\Events\\ChatMessageSent", object :
			PrivateChannelEventListener {
			override fun onEvent(event: PusherEvent?) {
				val json = JSONObject(event!!.data)
				val gson = Gson()
				val messageItem = gson.fromJson(json.toString(), EventReceiverItem::class.java)
				if (selectedChat.id == messageItem.chat_id && messageItem.sender.id != organization.id) {
					listPinnedMessages.add(messageItem.message)
					listPinnedMessages.sortBy { it.date }
					adapter = MessagesAdapter(listPinnedMessages.filter { it.pinned } as ArrayList<MessageItem>, this@PinnedMessagesActivity)
					runOnUiThread {
						binding.allPinnedMesagesChat.adapter = adapter
						binding.allPinnedMesagesChat.scrollToPosition(adapter.itemCount - 1)
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
	}
	private fun returnData() {
		val intent = Intent()
		setResult(RESULT_OK, intent)
		finish() // Закрываем активность
	}
	private fun getMessages(){
		listPinnedMessages = ArrayList()
		val request = Request.Builder()
			.url("$BASE_URL$CHATS/${selectedChat.id}")
			.addHeader(HEADER_TXT, HEADER_VALUE)
			.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
			.get()
			.build()
		client.newCall(request).enqueue(object : Callback {
			override fun onResponse(call: Call, response: Response) {
				if (response.isSuccessful) {
					response.use { it ->
						val responseBody = it.body?.string() // Читаем тело ответа
						val json = JSONObject(responseBody!!)
						val gson = Gson()
						val chatData = gson.fromJson(json.toString(), ChatDataItem::class.java)
						Log.e("RESPONSE_CHATDATA", chatData.toString())
						listPinnedMessages.addAll(chatData.messages)
						listPinnedMessages.sortBy { it.date }
						runOnUiThread {
							adapter = MessagesAdapter(listPinnedMessages.filter { it.pinned } as ArrayList<MessageItem>, this@PinnedMessagesActivity)
							binding.allPinnedMesagesChat.adapter = adapter
							binding.allPinnedMesagesChat.scrollToPosition(adapter.itemCount - 1)
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
		val list = listPinnedMessages.filter { it.pinned }
		if (list.isEmpty()) {
			returnData()
		}
	}
	override fun onItemClick(position: Int) {
		currentMessage = listPinnedMessages.filter { it.pinned }[position]
		if (currentMessage.pinned) {
			pinTxt.text = getString(R.string.unpin)
		} else {
			pinTxt.text = getString(R.string.pin)
		}
		showPopup()
	}
}

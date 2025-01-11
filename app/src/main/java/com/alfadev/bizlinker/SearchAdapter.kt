package com.alfadev.bizlinker

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alfadev.bizlinker.AddEditProductActivity.Companion.editProductItem
import com.alfadev.bizlinker.MainActivity.Companion.BASE_URL
import com.alfadev.bizlinker.MainActivity.Companion.CHATS
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_TXT
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.PRODUCTS
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_TXT
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE_START
import com.alfadev.bizlinker.MainActivity.Companion.client
import com.alfadev.bizlinker.SelectedChatActivity.Companion.selectedChat
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SearchAdapter(private val context: Activity, private val searchList: ArrayList<OrganizationItem>): RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
	inner class SearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		val userNameTxt: TextView = itemView.findViewById(R.id.searchNameTxt)
		val descriptionTxt: TextView = itemView.findViewById(R.id.searchDescriptionTxt)
		val logoTxt: TextView = itemView.findViewById(R.id.searchLogoTxt)
		val searchContact: Button = itemView.findViewById(R.id.searchContact)
		val logo: MaterialCardView = itemView.findViewById(R.id.cardviewIconSearch)
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
		val view: View = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
		return SearchViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
		val searchItem = searchList[position]
		holder.userNameTxt.text = searchItem.name
		holder.descriptionTxt.text = searchItem.inn
		
		var correctName = searchItem.name
		correctName = correctName.replace("\"","")
		correctName = correctName.replace(" ", "")
		correctName = try {
			correctName.substring(3, 6)
		} catch(ex:Exception){
			correctName.substring(3,correctName.length)
		}
		holder.logoTxt.text = correctName
		holder.searchContact.setOnClickListener {
			val requestBody = FormBody.Builder()
				.add("receiver_id", searchItem.id.toString()).build()
			val request = Request.Builder()
				.url("$BASE_URL$CHATS")
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
							openChat(searchItem)
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
		}
	}
	
	override fun getItemCount(): Int {
		return searchList.size
	}
	private  fun openChat(item: OrganizationItem){
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
							if (chatItem.receiver.id == item.id) {
								selectedChat = chatItem
								break
							}
						}
						context.runOnUiThread {
							val myIntent = Intent(context, SelectedChatActivity::class.java)
							context.startActivity(myIntent)
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
}
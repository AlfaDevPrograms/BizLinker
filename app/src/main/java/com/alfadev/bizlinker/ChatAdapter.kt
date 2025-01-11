package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ChatAdapter(
	private val messageList: ArrayList<ChatItem>,
	private val itemClickListener: OnItemClickListenerChat,
	private val longItemClickListener: OnLongItemClickListenerChat
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
	interface OnItemClickListenerChat {
		fun onItemClickChat(position: Int)
	}
	
	interface OnLongItemClickListenerChat {
		fun onLongItemClickChat(position: Int)
	}
	
	inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val userNameTxt: TextView = itemView.findViewById(R.id.usernameTxt)
		val datetimeTxt: TextView = itemView.findViewById(R.id.datetimeTxt)
		val messageTxt: TextView = itemView.findViewById(R.id.messageTxt)
		val logoTxt: TextView = itemView.findViewById(R.id.logoTxt)
		
		init {
			itemView.setOnClickListener {
				val position = bindingAdapterPosition
				if (position != RecyclerView.NO_POSITION) {
					itemClickListener.onItemClickChat(position)
				}
			}
			itemView.setOnLongClickListener {
				val position = bindingAdapterPosition
				if (position != RecyclerView.NO_POSITION) {
					longItemClickListener.onLongItemClickChat(position)
					true // Возвращаем true, чтобы указать, что событие обработано
				} else {
					false // Возвращаем false, если позиция некорректна
				}
			}
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
		val view: View =
			LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
		return ChatViewHolder(view)
	}
	
	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
		val messageItem = messageList[position]
		holder.userNameTxt.text = messageItem.receiver.name
		if (messageItem.last_message?.message?.isNotEmpty() == true) {
			holder.messageTxt.text = messageItem.last_message.message
		} else {
			holder.messageTxt.text = "Напишите первое сообщение!"
		}
		var correctName = messageItem.receiver.name
		correctName = correctName.replace("\"", "")
		correctName = correctName.replace(" ", "")
		correctName = try {
			correctName.substring(3, 6)
		} catch (ex: Exception) {
			correctName.substring(3, correctName.length)
		}
		try {
			val zonedDateTimeUTC = ZonedDateTime.parse(messageItem.last_message.date)
			val localZoneId = ZoneId.systemDefault()
			val zonedDateTimeLocal = zonedDateTimeUTC.withZoneSameInstant(localZoneId)
			val formatter = DateTimeFormatter.ofPattern("HH:mm")
			val formattedDate = zonedDateTimeLocal.format(formatter)
			holder.datetimeTxt.text = formattedDate
		} catch (ex: Exception) {
			holder.datetimeTxt.text = ""
		}
		
		holder.logoTxt.text = correctName
	}
	
	override fun getItemCount(): Int {
		return messageList.size
	}
}
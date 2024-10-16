package com.alfadev.bizlinker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messageList: ArrayList<ChatItem>, private val itemClickListener: OnItemClickListener): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
	interface OnItemClickListener {
		fun onItemClick(position: Int)
	}
	
	inner class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		val userNameTxt: TextView = itemView.findViewById(R.id.usernameTxt)
		val messageTxt: TextView = itemView.findViewById(R.id.messageTxt)
		val logoTxt: TextView = itemView.findViewById(R.id.logoTxt)
		
		init {
			itemView.setOnClickListener {
				val position = bindingAdapterPosition
				if(position != RecyclerView.NO_POSITION) {
					itemClickListener.onItemClick(position)
				}
			}
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
		val view: View = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
		return ChatViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
		val messageItem = messageList[position]
		holder.userNameTxt.text = messageItem.targetOrganization.name
		holder.messageTxt.text = messageItem.createAt.toString()
		holder.logoTxt.text = messageItem.targetOrganization.name.substring(0, 2)
	}
	
	override fun getItemCount(): Int {
		return messageList.size
	}
}
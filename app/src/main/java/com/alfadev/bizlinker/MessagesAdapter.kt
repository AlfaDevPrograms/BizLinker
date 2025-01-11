package com.alfadev.bizlinker

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alfadev.bizlinker.LoginActivity.Companion.organization
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MessagesAdapter(private val items: ArrayList<MessageItem>, private val itemClickListener: OnItemClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	interface OnItemClickListener {
		fun onItemClick(position: Int)
	}
	
	override fun getItemViewType(position: Int): Int {
		// Определите условие для выбора типа элемента
		return if (items[position].sender_id == organization.id) {
			VIEW_TYPE_RIGHT
		} else {
			VIEW_TYPE_LEFT
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return if (viewType == VIEW_TYPE_RIGHT) {
			val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item_right, parent, false)
			RightViewHolder(view)
		} else {
			val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item_left, parent, false)
			LeftViewHolder(view)
		}
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val item = items[position]
		when (holder) {
			is LeftViewHolder -> holder.bind(item)
			is RightViewHolder -> holder.bind(item)
		}
	}
	
	override fun getItemCount(): Int {
		return items.size
	}
	
	inner class LeftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val message: TextView = itemView.findViewById(R.id.messageItemTxt)
		private val date: TextView = itemView.findViewById(R.id.datetimeItemTxt)
		private val pin: ImageButton = itemView.findViewById(R.id.messageItemPin)
		init {
			itemView.setOnClickListener {
				val position = bindingAdapterPosition
				if(position != RecyclerView.NO_POSITION) {
					itemClickListener.onItemClick(position)
				}
			}
		}
		fun bind(item: MessageItem) {
			message.text = item.message
			try
			{
				val zonedDateTimeUTC = ZonedDateTime.parse(item.date)
				val localZoneId = ZoneId.systemDefault()
				val zonedDateTimeLocal = zonedDateTimeUTC.withZoneSameInstant(localZoneId)
				val formatter = DateTimeFormatter.ofPattern("HH:mm")
				val formattedDate = zonedDateTimeLocal.format(formatter)
				date.text = formattedDate
				if (item.pinned) {
					pin.visibility = VISIBLE
				} else {
					pin.visibility = GONE
				}
			}
			catch(ex:Exception){
				date.text = ""
			}
		}
	}
	
	inner class RightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val message: TextView = itemView.findViewById(R.id.messageItemTxt)
		private val date: TextView = itemView.findViewById(R.id.datetimeItemTxt)
		private val pin: ImageButton = itemView.findViewById(R.id.messageItemPin)
		init {
			itemView.setOnClickListener {
				val position = bindingAdapterPosition
				if(position != RecyclerView.NO_POSITION) {
					itemClickListener.onItemClick(position)
				}
			}
		}
		fun bind(item: MessageItem) {
			message.text = item.message
			try
			{
				val zonedDateTimeUTC = ZonedDateTime.parse(item.date)
				val localZoneId = ZoneId.systemDefault()
				val zonedDateTimeLocal = zonedDateTimeUTC.withZoneSameInstant(localZoneId)
				val formatter = DateTimeFormatter.ofPattern("HH:mm")
				val formattedDate = zonedDateTimeLocal.format(formatter)
				date.text = formattedDate
				if (item.pinned) {
					pin.visibility = VISIBLE
				} else {
					pin.visibility = GONE
				}
			}
			catch(ex:Exception){
				date.text = ""
			}

		}
	}
	
	companion object {
		private const val VIEW_TYPE_LEFT = 0
		private const val VIEW_TYPE_RIGHT = 1
	}
}
package com.alfadev.bizlinker

data class MessageItem(val id: Long, val message: String, val date: String, val sender_id: Long, val status: Boolean, var pinned: Boolean)

package com.alfadev.bizlinker

data class EventReceiverItem(
	val chat_id: Long,
	val sender: OrganizationItem,
	val message: MessageItem
)

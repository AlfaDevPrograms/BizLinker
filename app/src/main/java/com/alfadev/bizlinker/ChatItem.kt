package com.alfadev.bizlinker

data class ChatItem
	(val id: Long,
	 val createAt: Long,
	 val receiver: OrganizationItem,
	 val last_message: LastMessageItem,
	 val isPin: Boolean)

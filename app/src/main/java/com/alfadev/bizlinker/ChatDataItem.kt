package com.alfadev.bizlinker

import java.util.ArrayList

data class ChatDataItem(
	val id: Long,
	val my_id: Long,
	val members: List<OrganizationItem>,
	val messages: List<MessageItem>,
)

package com.alfadev.bizlinker

data class MessageItem(val id: Long, val content: String, val createdAt: Long, val senderOrganization: OrganizationItem, val isRead: Boolean)

package com.alfadev.bizlinker

data class ChatItem(val id: Long, val createAt: Long, val targetOrganization: OrganizationItem, val isPin: Boolean)

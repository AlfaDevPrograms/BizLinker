package com.alfadev.bizlinker

data class WishlistItem(val id: Long, val name: String, val count: Int, val price: Double, var organization: OrganizationItem)

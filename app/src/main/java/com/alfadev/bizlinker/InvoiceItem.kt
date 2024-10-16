package com.alfadev.bizlinker

data class InvoiceItem(val id: Long, val createAt: Long, val organization: OrganizationItem, val products: ArrayList<InvoiceProductItem>?, val summaryPrice: Double)

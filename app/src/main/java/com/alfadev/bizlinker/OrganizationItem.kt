package com.alfadev.bizlinker

data class OrganizationItem(val id: Long, val name: String, val ogrnNumber: String, val kppNumber: String, val innNumber: String, val okpoCode: String, val address: String, val password: String, val emails: ArrayList<EmailItem>, val phones: ArrayList<PhoneItem>, val products: ArrayList<ProductItem>?, val form: OrganizationForm, val website: String?)
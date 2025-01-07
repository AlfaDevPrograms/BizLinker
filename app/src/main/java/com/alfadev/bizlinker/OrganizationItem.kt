package com.alfadev.bizlinker

data class OrganizationItem
	(val id: Long,
	 val name: String,
	 val ogrn: String,
	 val kpp: String,
	 val inn: String,
	 val okpo: String,
	 val address: String,
	 val password: String,
	 val emails: ArrayList<EmailItem>,
	 val phones: ArrayList<PhoneItem>,
	 val products: ArrayList<ProductItem>?,
	 val type: String,
	 val website: String?)
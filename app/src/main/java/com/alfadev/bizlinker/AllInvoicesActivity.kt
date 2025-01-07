package com.alfadev.bizlinker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.databinding.ActivityAllInvoicesBinding

class AllInvoicesActivity: AppCompatActivity(), InvoiceAdapter.OnItemClickListener {
	//Биндинг
	private lateinit var binding: ActivityAllInvoicesBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		sharedPreferences = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
		MainActivity.setTheme(this) //Создание самого проекта и привязки!
		super.onCreate(savedInstanceState)
		binding = ActivityAllInvoicesBinding.inflate(layoutInflater)
		setContentView(binding.root)
		val listArray: ArrayList<InvoiceItem> = ArrayList()
		for(i in 0 .. 10) {
			listArray.add(InvoiceItem(i.toLong(), i.toLong(), OrganizationItem(i.toLong(), getString(R.string.username_chat_item_txt_hint) + " $i", i.toString(), i.toString(), i.toString(), i.toString(), i.toString(), i.toString(), arrayListOf(EmailItem(i.toLong(), "")), arrayListOf(PhoneItem(i.toLong(), "")), null,"", null), null, 1000.00 + i))
		}
		binding.allInvoices.layoutManager = LinearLayoutManager(this)
		binding.allInvoices.adapter = InvoiceAdapter(listArray, this)
		binding.backToChatsFromInvocies.setOnClickListener {
			finish()
		}
	}
	
	override fun onItemClick(position: Int) {
		val myIntent = Intent(this@AllInvoicesActivity, InvoiceActivity::class.java)
		startActivity(myIntent)
	}
}
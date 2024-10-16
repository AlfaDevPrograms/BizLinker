package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfadev.bizlinker.InvoiceActivity.Companion.hideKeyboard
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.SelectedChatActivity.Companion.selectedChat
import com.alfadev.bizlinker.databinding.ChatFragmentBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatFragment: Fragment(), ChatAdapter.OnItemClickListener {
	private var param1: String? = null
	private var param2: String? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			param1 = it.getString(ARG_PARAM1)
			param2 = it.getString(ARG_PARAM2)
		}
	}
	//Биндинг
	private lateinit var binding: ChatFragmentBinding
	private lateinit var listArray: ArrayList<ChatItem>
	@SuppressLint("UseCompatLoadingForDrawables")
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = ChatFragmentBinding.inflate(inflater, container, false)
		binding.searchUsers.setOnClickListener {
			if(binding.chatHeaderTxt.visibility == VISIBLE) {
				binding.chatHeaderTxt.visibility = INVISIBLE
				binding.searchChatET.visibility = VISIBLE
				binding.searchChatET.requestFocus()
				val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
				imm?.showSoftInput(binding.searchChatET, InputMethodManager.SHOW_IMPLICIT)
			}
			else {
				if(binding.searchChatET.isFocused) {
					hideKeyboard(binding.searchChatET)
				}
				else {
					binding.searchChatET.requestFocus()
					val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
					imm?.showSoftInput(binding.searchChatET, InputMethodManager.SHOW_IMPLICIT)
				}
				if(binding.searchChatET.text.isNullOrEmpty()) {
					binding.chatHeaderTxt.visibility = VISIBLE
					binding.searchChatET.visibility = INVISIBLE
				}
			}
		}
		binding.searchChatET.setOnEditorActionListener { _, actionId, _ ->
			if(actionId == EditorInfo.IME_ACTION_SEARCH) {
				hideKeyboard(binding.searchChatET)
				if(binding.searchChatET.text.isNullOrEmpty()) {
					binding.chatHeaderTxt.visibility = VISIBLE
					binding.searchChatET.visibility = INVISIBLE
				}
				true
			}
			else {
				false
			}
		}
		binding.searchChatET.doAfterTextChanged {
			val newList = listArray.filter { it.targetOrganization.name.lowercase().startsWith(binding.searchChatET.text.toString().lowercase()) }
			binding.chats.adapter = ChatAdapter(newList as ArrayList<ChatItem>, this)
		}
		if(sharedPreferences.getString("role", "provider") == "provider") {
			binding.buttonAllInvoices.visibility = VISIBLE
		}
		else {
			binding.buttonAllInvoices.visibility = INVISIBLE
		}
		listArray = ArrayList()
		for(i in 0 .. 10) {
			listArray.add(ChatItem(i.toLong(), i.toLong(), OrganizationItem(i.toLong(), getString(R.string.username_chat_item_txt_hint) + " $i", i.toString(), i.toString(), i.toString(), i.toString(), i.toString(), i.toString(), arrayListOf(EmailItem(i.toLong(), "")), arrayListOf(PhoneItem(i.toLong(), "")), null, OrganizationForm(i.toLong(), ""), null), false))
		}
		binding.chats.layoutManager = LinearLayoutManager(this.context)
		binding.chats.adapter = ChatAdapter(listArray, this)
		binding.buttonAllInvoices.setOnClickListener {
			val myIntent = Intent(this@ChatFragment.context, AllInvoicesActivity::class.java)
			startActivity(myIntent)
		}
		return binding.root
	}
	
	override fun onItemClick(position: Int) {
		val chat = listArray[position]
		selectedChat = chat
		val myIntent = Intent(this@ChatFragment.context, SelectedChatActivity::class.java)
		startActivity(myIntent)
	}
}
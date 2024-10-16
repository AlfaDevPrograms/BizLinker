package com.alfadev.bizlinker

import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.databinding.ActivitySelectedChatBinding

class SelectedChatActivity: AppCompatActivity() {
	companion object {
		lateinit var selectedChat: ChatItem
	}
	//Биндинг
	private lateinit var binding: ActivitySelectedChatBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		sharedPreferences = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
		MainActivity.setTheme(this) //Создание самого проекта и привязки!
		super.onCreate(savedInstanceState)
		binding = ActivitySelectedChatBinding.inflate(layoutInflater)
		setContentView(binding.root)
		if(sharedPreferences.getString("role", "provider") == "provider") {
			binding.addInvoice.visibility = VISIBLE
		}
		else {
			binding.addInvoice.visibility = INVISIBLE
		}
		binding.selectedChatUsernameTxt.text = selectedChat.targetOrganization.name
		binding.backToChats.setOnClickListener {
			finish()
		}
		binding.selectedChatMessage.doAfterTextChanged {
			if(it.isNullOrEmpty()) {
				if(binding.attach.visibility == INVISIBLE) {
					binding.attach.visibility = VISIBLE
					binding.send.visibility = INVISIBLE
				}
			}
			else {
				if(binding.send.visibility == INVISIBLE) {
					binding.send.visibility = VISIBLE
					binding.attach.visibility = INVISIBLE
				}
			}
		}
		binding.send.setOnClickListener {
			var message = binding.selectedChatMessage.text.toString()
			message = message.replace(Regex("^[\n\r]+|[\n\r]+$"), "").trim()
			Log.e("mes", message)
			binding.selectedChatMessage.text.clear()
			binding.attach.visibility = VISIBLE
			binding.send.visibility = INVISIBLE
		}
	}
}

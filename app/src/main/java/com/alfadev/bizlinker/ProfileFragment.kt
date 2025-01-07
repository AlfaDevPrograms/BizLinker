package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alfadev.bizlinker.LoginActivity.Companion.organization
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.databinding.ProfileFragmentBinding
import com.google.android.material.snackbar.Snackbar

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {
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
	private lateinit var binding: ProfileFragmentBinding
	
	@SuppressLint("UseCompatLoadingForDrawables")
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = ProfileFragmentBinding.inflate(inflater, container, false)
		binding.textName.text = organization.name
		binding.textOgrnNumber.text = organization.ogrn
		binding.textKppNumber.text = organization.kpp
		binding.textInnNumber.text = organization.inn
		binding.textOkpoCode.text = organization.okpo
		binding.textAddress.text = organization.address
		binding.textForm.text = organization.type
		binding.textWebsite.text = organization.website
		binding.textPassword.text = organization.password
		var emailsString = ""
		var phonesString = ""
		for (item in organization.emails) {
			emailsString += item.name + "\n"
		}
		for (item in organization.phones) {
			phonesString += item.name + "\n"
		}
		binding.textEmails.text = emailsString
		binding.textPhones.text = phonesString
		binding.themeLight.setOnClickListener {
			sharedPreferences.edit().putString("theme", "light").apply()
			MainActivity.themeChange = true
			this.requireActivity().recreate()
		}
		binding.themeDark.setOnClickListener {
			sharedPreferences.edit().putString("theme", "dark").apply()
			MainActivity.themeChange = true
			this.requireActivity().recreate()
		}
		binding.themeGreen.setOnClickListener {
			sharedPreferences.edit().putString("theme", "green").apply()
			MainActivity.themeChange = true
			this.requireActivity().recreate()
		}
		binding.themeBlue.setOnClickListener {
			sharedPreferences.edit().putString("theme", "blue").apply()
			MainActivity.themeChange = true
			this.requireActivity().recreate()
		}
		binding.themeDark2.setOnClickListener {
			sharedPreferences.edit().putString("theme", "dark2").apply()
			MainActivity.themeChange = true
			this.requireActivity().recreate()
		}
		binding.themeDark3.setOnClickListener {
			sharedPreferences.edit().putString("theme", "dark3").apply()
			MainActivity.themeChange = true
			this.requireActivity().recreate()
		}
		binding.switchRole.isChecked = sharedPreferences.getString("role", "provider") == "provider"
		binding.switchRole.setOnCheckedChangeListener { _, b ->
			if (b) {
				sharedPreferences.edit().putString("role", "provider").apply()
				Snackbar.make(
					binding.profileHeaderTxt,
					getString(R.string.change_to_provider),
					Snackbar.LENGTH_SHORT
				).show()
			} else {
				sharedPreferences.edit().putString("role", "buyer").apply()
				Snackbar.make(
					binding.profileHeaderTxt,
					getString(R.string.change_to_buyer),
					Snackbar.LENGTH_SHORT
				).show()
			}
		}
		return binding.root
	}
}
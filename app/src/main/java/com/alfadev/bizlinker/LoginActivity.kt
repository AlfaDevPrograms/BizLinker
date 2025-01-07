package com.alfadev.bizlinker

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
import android.util.Log
import android.util.TypedValue
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.alfadev.bizlinker.MainActivity.Companion.BASE_URL
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_TXT
import com.alfadev.bizlinker.MainActivity.Companion.HEADER_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.REGISTER
import com.alfadev.bizlinker.MainActivity.Companion.SIGN_UP
import com.alfadev.bizlinker.MainActivity.Companion.TOKEN_VALUE
import com.alfadev.bizlinker.MainActivity.Companion.client
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import com.alfadev.bizlinker.R

class LoginActivity : AppCompatActivity() {
	companion object {
		lateinit var organization: OrganizationItem
	}
	private fun isOnline(context: Context): Boolean {
		val connectivityManager =
			context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val activeNetwork =
			connectivityManager.activeNetwork
		val networkCapabilities =
			connectivityManager.getNetworkCapabilities(activeNetwork)
		return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
	}
	//Биндинг
	private lateinit var binding: ActivityLoginBinding
	private var passwordVisible: Boolean = false
	private var isRegister: Boolean = false
	override fun onCreate(savedInstanceState: Bundle?) {
		sharedPreferences = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
		MainActivity.setTheme(this) //Создание самого проекта и привязки!
		super.onCreate(savedInstanceState)
		binding = ActivityLoginBinding.inflate(layoutInflater)
		setContentView(binding.root)
		val loadAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.loading)
		val dialog = Dialog(this)
		dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialog.setCancelable(false)
		dialog.setContentView(R.layout.dialog_load)
		val connectivityManager =
			this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val networkCallback = object : ConnectivityManager.NetworkCallback() {
			override fun onAvailable(network: Network) {
				super.onAvailable(network)
				runOnUiThread {
					if (!this@LoginActivity.isFinishing) {
						dialog.dismiss()
					}
				}
			}
			
			override fun onLost(network: Network) {
				super.onLost(network)
				runOnUiThread {
					if (!this@LoginActivity.isFinishing) {
						dialog.findViewById<ImageButton>(R.id.load).startAnimation(loadAnimation)
						dialog.show()
					}
				}
			}
		}
		connectivityManager.registerDefaultNetworkCallback(networkCallback)
		binding.login.setOnClickListener {
			binding.emailTxt.error = ""
			binding.passwordTxt.error = ""
			binding.showPassword.visibility = VISIBLE
			if (binding.email.text!!.isEmpty()) {
				binding.emailTxt.error = getString(R.string.error_input)
			}
			if (binding.password.text!!.isEmpty()) {
				binding.showPassword.visibility = INVISIBLE
				binding.passwordTxt.error = getString(R.string.error_input)
			} else if (binding.email.text!!.isNotEmpty() && binding.password.text!!.isNotEmpty()) {
				client = OkHttpClient()
				val requestBody = FormBody.Builder().add("inn", binding.email.text.toString())
					.add("password", binding.password.text.toString()).build()
				if (isRegister) {
					val request = Request.Builder().url("$BASE_URL$REGISTER")
						.addHeader(HEADER_TXT, HEADER_VALUE).post(requestBody).build()
					client.newCall(request).enqueue(object : Callback {
						override fun onResponse(call: Call, response: Response) {
							if (response.isSuccessful) {
								binding.signIn.performClick()
								binding.login.performClick()
							} else {
								response.use {
									val responseBody = it.body?.string() // Читаем тело ответа
									val json = JSONObject(responseBody!!)
									runOnUiThread {
										try {
											binding.showPassword.visibility = INVISIBLE
											binding.passwordTxt.error = json.getJSONObject("errors")
												.getJSONArray("password")[0].toString()
										} catch (_: Exception) {
											binding.showPassword.visibility = VISIBLE
										}
										try {
											binding.emailTxt.error = json.getJSONObject("errors")
												.getJSONArray("inn")[0].toString()
										} catch (_: Exception) {
										}
									}
								}
							}
						}
						
						override fun onFailure(call: Call, e: IOException) {
							runOnUiThread {
								Snackbar.make(
									binding.welcomeTxt,
									getString(R.string.smth_went_wrong),
									Snackbar.LENGTH_SHORT
								).show()
							}
						}
					})
				} else {
					val request = Request.Builder().url("$BASE_URL$SIGN_UP")
						.addHeader(HEADER_TXT, HEADER_VALUE).post(requestBody).build()
					client.newCall(request).enqueue(object : Callback {
						override fun onResponse(call: Call, response: Response) {
							if (response.isSuccessful) {
								response.use {
									val responseBody = it.body?.string() // Читаем тело ответа
									val json = JSONObject(responseBody!!)
									TOKEN_VALUE = json.getString("access_token")
									sharedPreferences.edit()
										.putString("token",TOKEN_VALUE).apply()
									sharedPreferences.edit()
										.putBoolean("isRemember", binding.remember.isChecked).apply()
									sharedPreferences.edit()
										.putString("login", binding.email.text.toString()).apply()
									sharedPreferences.edit()
										.putString("password", binding.password.text.toString()).apply()
									val myIntent = Intent(this@LoginActivity, MainActivity::class.java)
									startActivity(myIntent)
									finish()
								}
							} else {
								response.use {
									val responseBody = it.body?.string() // Читаем тело ответа
									val json = JSONObject(responseBody!!)
									runOnUiThread {
										try {
											binding.showPassword.visibility = INVISIBLE
											binding.passwordTxt.error = json.getJSONObject("errors")
												.getJSONArray("password")[0].toString()
										} catch (_: Exception) {
											binding.showPassword.visibility = VISIBLE
										}
										try {
											binding.emailTxt.error = json.getJSONObject("errors")
												.getJSONArray("inn")[0].toString()
										} catch (_: Exception) {
										}
									}
								}
							}
						}
						
						override fun onFailure(call: Call, e: IOException) {
							runOnUiThread {
								Toast.makeText(
									this@LoginActivity,
									getString(R.string.smth_went_wrong),
									Toast.LENGTH_SHORT
								).show()
							}
						}
					})
				}
			}
		}
		binding.showPassword.setOnClickListener {
			if (passwordVisible) {
				binding.showPassword.foreground =
					AppCompatResources.getDrawable(this, R.drawable.unshow_password)
				binding.password.inputType =
					TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_NO_SUGGESTIONS or TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
			} else {
				binding.showPassword.foreground =
					AppCompatResources.getDrawable(this, R.drawable.show_password)
				binding.password.inputType =
					TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_NO_SUGGESTIONS or TYPE_TEXT_VARIATION_PASSWORD
			}
			passwordVisible = !passwordVisible
			binding.password.setSelection(binding.password.text!!.length)
			binding.password.invalidate()
		}
		binding.email.doAfterTextChanged {
			if (it!!.isNotEmpty()) {
				if (!binding.emailTxt.error.isNullOrEmpty()) {
					binding.emailTxt.error = getString(R.string.empty)
				}
			}
		}
		binding.password.doAfterTextChanged {
			if (it!!.isNotEmpty()) {
				if (!binding.passwordTxt.error.isNullOrEmpty()) {
					binding.passwordTxt.error = getString(R.string.empty)
					binding.showPassword.visibility = VISIBLE
				}
			}
		}
		val typedValue = TypedValue()
		theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
		val colorOnPrimary = typedValue.data
		val colorStateList = ColorStateList(
			arrayOf(
				intArrayOf(android.R.attr.state_pressed),
				intArrayOf(android.R.attr.state_selected),
				intArrayOf(android.R.attr.state_focused),
				intArrayOf() // Неактивное состояние
			), intArrayOf(
				colorOnPrimary, // Цвет при нажатии
				colorOnPrimary, // Цвет при выборе
				colorOnPrimary, // Цвет при фокусе
				colorOnPrimary // Цвет в неактивном состоянии
			)
		)
		binding.signIn.setOnClickListener {
			binding.emailTxt.error = ""
			binding.passwordTxt.error = ""
			binding.showPassword.visibility = VISIBLE
			isRegister = false
			binding.register.strokeColor = colorStateList
			binding.signIn.strokeColor = ContextCompat.getColorStateList(this, R.color.red)
			binding.login.text = getString(R.string.sign_in_button_continue)
		}
		binding.register.setOnClickListener {
			binding.emailTxt.error = ""
			binding.passwordTxt.error = ""
			binding.showPassword.visibility = VISIBLE
			isRegister = true
			binding.register.strokeColor = ContextCompat.getColorStateList(this, R.color.red)
			binding.signIn.strokeColor = colorStateList
			binding.login.text = getString(R.string.register_button_continue)
		}
		binding.errorSignIn.setOnClickListener {
			Snackbar.make(
				binding.errorSignIn, getString(R.string.error_signin), Snackbar.LENGTH_LONG
			).show()
		}
		binding.password.setOnEditorActionListener { _, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				binding.login.performClick()
			}
			true
		}
		binding.showPassword.performClick()
		binding.signIn.performClick()
		if (sharedPreferences.getBoolean("isRemember", false)) {
			val myIntent = Intent(this@LoginActivity, MainActivity::class.java)
			startActivity(myIntent)
			finish()
		}
		if (!dialog.isShowing) {
			if (!isOnline(this)) {
				dialog.findViewById<ImageButton>(R.id.load).startAnimation(loadAnimation)
				dialog.show()
			}
		}
	}
}
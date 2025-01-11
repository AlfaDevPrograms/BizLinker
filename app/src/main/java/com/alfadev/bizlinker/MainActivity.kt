package com.alfadev.bizlinker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.alfadev.bizlinker.LoginActivity.Companion.organization
import com.alfadev.bizlinker.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.gson.Gson
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpAuthorizer
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
	interface OnMyEventListener {
		fun onEventOccurred(message: String)
	}
	
	class EventGenerator {
		var listener: OnMyEventListener? = null
		fun triggerEvent() {            // Генерация события
			listener!!.onEventOccurred("Событие произошло!")
		}
	}
	//Биндинг
	private lateinit var binding: ActivityMainBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		sharedPreferences = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
		setTheme(this)
		//Создание самого проекта и привязки!
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		//Весь остальной код, выше не трогать!!!
		val loadAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.loading)
		val dialog = Dialog(this)
		dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialog.setCancelable(false)
		dialog.setContentView(R.layout.dialog_load)
		client = OkHttpClient()
		TOKEN_VALUE = sharedPreferences.getString("token", "")!!
		val request = Request.Builder().url("$BASE_URL$GET_ME")
			.addHeader(HEADER_TXT, HEADER_VALUE)
			.addHeader(TOKEN_TXT, "$TOKEN_VALUE_START$TOKEN_VALUE")
			.build()
		client.newCall(request).enqueue(object : Callback {
			override fun onResponse(call: Call, response: Response) {
				if (response.isSuccessful) {
					response.use {
						val responseBody = it.body?.string() // Читаем тело ответа
						val json = JSONObject(responseBody!!)
						val gson = Gson()
						organization = gson.fromJson(json.toString(), OrganizationItem::class.java)
						val header = HashMap<String, String>()
						header[TOKEN_TXT] = "$TOKEN_VALUE_START$TOKEN_VALUE"
						val authorizer = HttpAuthorizer("http://bizlinker.tw1.ru/broadcasting/auth")
						authorizer.setHeaders(header)
						val options = PusherOptions().apply {
							setAuthorizer(authorizer)
							setHost("bizlinker.tw1.ru")
							setWsPort(8080)
							setWssPort(8080)
							setUseTLS(false)
						}
						val pusher = Pusher("zlmotneylavu7ctww1oq", options)
						pusher.connect(object : ConnectionEventListener {
							override fun onConnectionStateChange(change: ConnectionStateChange?) {
								Log.d("Pusher", "Connection state changed to: ${change?.currentState}")
								if (change?.currentState == ConnectionState.CONNECTED) {
									channel = pusher.subscribePrivate("private-chats.${organization.id}")
									val eventGenerator = EventGenerator()
									eventGenerator.listener = ChatFragment.onMyEventListener
									eventGenerator.triggerEvent()
								}
							}
							
							override fun onError(message: String?, code: String?, e: Exception?) {
								Log.e("Pusher Error", "Error: $message Code: $code", e)
							}
						})
					}
				} else {
					runOnUiThread {
						if (!this@MainActivity.isFinishing) {
							dialog.findViewById<ImageButton>(R.id.load).startAnimation(loadAnimation)
							dialog.show()
						}
					}
					Log.e("RESPONSE", response.toString())
				}
			}
			
			override fun onFailure(call: Call, e: IOException) {
				runOnUiThread {
					if (!this@MainActivity.isFinishing) {
						dialog.findViewById<ImageButton>(R.id.load).startAnimation(loadAnimation)
						dialog.show()
					}
				}
				Log.e("RESPONSE", e.toString())
			}
		})
		val connectivityManager =
			this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val networkCallback = object : ConnectivityManager.NetworkCallback() {
			override fun onAvailable(network: Network) {
				super.onAvailable(network)
				runOnUiThread {
					if (!this@MainActivity.isFinishing) {
						dialog.dismiss()
					}
				}
			}
			
			override fun onLost(network: Network) {
				super.onLost(network)
				Toast.makeText(baseContext, "Потеряна связь!", Toast.LENGTH_LONG).show()
				runOnUiThread {
					if (!this@MainActivity.isFinishing) {
						dialog.findViewById<ImageButton>(R.id.load).startAnimation(loadAnimation)
						dialog.show()
					}
				}
			}
		}
		connectivityManager.registerDefaultNetworkCallback(networkCallback)
		binding.adContainerView.viewTreeObserver.addOnGlobalLayoutListener(object :
			ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				binding.adContainerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
				mBannerAd = loadBannerAd(getAdSize())
			}
		})
		val navHostFragment: NavHostFragment? =
			supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment?
		val navCo: NavController = navHostFragment!!.navController
		binding.bottomNavigationView.findViewById<BottomNavigationItemView>(R.id.products_button)
			.setOnLongClickListener { true }
		binding.bottomNavigationView.findViewById<BottomNavigationItemView>(R.id.chat_button)
			.setOnLongClickListener { true }
		binding.bottomNavigationView.findViewById<BottomNavigationItemView>(R.id.search_button)
			.setOnLongClickListener { true }
		binding.bottomNavigationView.findViewById<BottomNavigationItemView>(R.id.profile_button)
			.setOnLongClickListener { true }
		if (themeChange) {
			themeChange = false
			binding.bottomNavigationView.selectedItemId = R.id.profile_button
			navCo.navigate(R.id.action_profile_self)
		} else {
			binding.bottomNavigationView.selectedItemId = R.id.chat_button
			navCo.navigate(R.id.action_chat_self)
		}
		id = binding.bottomNavigationView.selectedItemId
		binding.bottomNavigationView.setOnItemSelectedListener { item ->
			when (item.itemId) {
				R.id.products_button -> {
					when (id) {
						R.id.products_button -> {
							navCo.navigate(R.id.action_products_self)
						}
						
						R.id.chat_button -> {
							navCo.navigate(R.id.action_chat_to_products)
						}
						
						R.id.search_button -> {
							navCo.navigate(R.id.action_search_to_products)
						}
						
						R.id.profile_button -> {
							navCo.navigate(R.id.action_profile_to_products)
						}
					}
					id = R.id.products_button
					item.isChecked = true
				}
				
				R.id.chat_button -> {
					when (id) {
						R.id.products_button -> {
							navCo.navigate(R.id.action_products_to_chat)
						}
						
						R.id.chat_button -> {
							navCo.navigate(R.id.action_chat_self)
						}
						
						R.id.search_button -> {
							navCo.navigate(R.id.action_search_to_chat)
						}
						
						R.id.profile_button -> {
							navCo.navigate(R.id.action_profile_to_chat)
						}
					}
					id = R.id.chat_button
					item.isChecked = true
				}
				
				R.id.search_button -> {
					when (id) {
						R.id.products_button -> {
							navCo.navigate(R.id.action_products_to_search)
						}
						
						R.id.chat_button -> {
							navCo.navigate(R.id.action_chat_to_search)
						}
						
						R.id.search_button -> {
							navCo.navigate(R.id.action_search_self)
						}
						
						R.id.profile_button -> {
							navCo.navigate(R.id.action_profile_to_search)
						}
					}
					id = R.id.search_button
					item.isChecked = true
				}
				
				R.id.profile_button -> {
					when (id) {
						R.id.products_button -> {
							navCo.navigate(R.id.action_products_to_profile)
						}
						
						R.id.chat_button -> {
							navCo.navigate(R.id.action_chat_to_profile)
						}
						
						R.id.search_button -> {
							navCo.navigate(R.id.action_search_to_profile)
						}
						
						R.id.profile_button -> {
							navCo.navigate(R.id.action_profile_self)
						}
					}
					id = R.id.profile_button
					item.isChecked = true
				}
			}
			false
		}
		onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				showExitConfirmationDialog()
			}
		})
	}
	
	private fun showExitConfirmationDialog() {
		val dlgAlert = AlertDialog.Builder(this, R.style.MyDialogThemeException)
		dlgAlert.setTitle("Подтвердите действие!")
		dlgAlert.setMessage("Вы действительно хотите выйти из приложения?")
		dlgAlert.setCancelable(true)
		dlgAlert.setPositiveButton("Нет") { _: DialogInterface?, _: Int -> }
		dlgAlert.setNegativeButton("Да") { _: DialogInterface?, _: Int ->
			finishAffinity()
		}
		val connDialog = dlgAlert.create()
		connDialog.setOnShowListener {
			val p = connDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			p.setTextColor(resources.getColor(R.color.whiteDark, this.theme))
			val n = connDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
			n.setTextColor(resources.getColor(R.color.whiteDark, this.theme))
		}
		connDialog.show()
	}
	
	//Объявление статической переменной
	companion object {
		lateinit var client: OkHttpClient
		const val BASE_URL = "http://bizlinker.tw1.ru/api"
		const val REGISTER = "/auth/register"
		const val SIGN_UP = "/auth/login"
		const val GET_ME = "/auth/my"
		const val GET_ORGANIZATION = "/organizations/"
		const val GET_PRODUCTS = "/products"
		const val PRODUCTS = "/my/products"
		const val WISHLIST = "/my/wishlist"
		const val ORGANIZATIONS = "/organizations"
		const val CHATS = "/chats"
		const val SEND_MESSAGE = "/messages/send"
		const val PIN_MESSAGE = "/messages/pin"
		const val UNPIN_MESSAGE = "/messages/unpin"
		const val HEADER_TXT = "Accept"
		const val HEADER_VALUE = "application/json"
		const val TOKEN_TXT = "Authorization"
		const val TOKEN_VALUE_START = "Bearer "
		var TOKEN_VALUE = ""
		lateinit var channel: PrivateChannel
		
		//Сохранение всех настроек
		lateinit var sharedPreferences: SharedPreferences
		var id = 0
		var themeChange = false
		
		//Метод смены темы
		fun setTheme(context: Context) {
			if (sharedPreferences.getString("theme", "light") == "light") {
				context.setTheme(R.style.Theme_BizLinker)
			} else if (sharedPreferences.getString("theme", "light") == "dark") {
				context.setTheme(R.style.Theme_BizLinkerDark)
			} else if (sharedPreferences.getString("theme", "light") == "green") {
				context.setTheme(R.style.ThemeGreen)
			} else if (sharedPreferences.getString("theme", "light") == "blue") {
				context.setTheme(R.style.ThemeBlue)
			} else if (sharedPreferences.getString("theme", "light") == "dark2") {
				context.setTheme(R.style.ThemeDark2)
			} else if (sharedPreferences.getString("theme", "light") == "dark3") {
				context.setTheme(R.style.ThemeDark3)
			}
		}
	}
	
	//Реклама
	private var mBannerAd: BannerAdView? = null
	private fun getAdSize(): BannerAdSize {
		val displayMetrics = resources.displayMetrics
		var adWidthPixels: Int = binding.adContainerView.width
		if (adWidthPixels == 0) {
			adWidthPixels = displayMetrics.widthPixels
		}
		val adWidth = (adWidthPixels / displayMetrics.density).roundToInt()
		return BannerAdSize.stickySize(this, adWidth)
	}
	
	//    private val rustoreAd = "R-M-6937787-1"
	//    private val huaweiAd = "R-M-7118410-1"
	//    private val xiaomiAd = "R-M-7118412-1"
	
	private fun loadBannerAd(adSize: BannerAdSize): BannerAdView {
		val bannerAd: BannerAdView = binding.adContainerView
		bannerAd.setAdSize(adSize)
		//bannerAd.setAdUnitId(rustoreAd)
		//bannerAd.setAdUnitId(huaweiAd)
		//bannerAd.setAdUnitId(xiaomiAd)
		bannerAd.setAdUnitId("demo-banner-yandex")
		bannerAd.setBannerAdEventListener(object : BannerAdEventListener {
			override fun onAdLoaded() {
				if (isDestroyed && mBannerAd != null) {
					mBannerAd!!.destroy()
				}
			}
			
			override fun onAdFailedToLoad(error: AdRequestError) {}
			override fun onAdClicked() {}
			override fun onLeftApplication() {}
			override fun onReturnedToApplication() {}
			override fun onImpression(impressionData: ImpressionData?) {}
		})
		val adRequest = AdRequest.Builder().build()
		bannerAd.loadAd(adRequest)
		return bannerAd
	}
}
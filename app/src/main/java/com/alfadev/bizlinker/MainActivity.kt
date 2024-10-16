package com.alfadev.bizlinker

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
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
import com.alfadev.bizlinker.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import okhttp3.OkHttpClient
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
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
				Toast.makeText(baseContext, "sdfglsdfgsd", Toast.LENGTH_LONG).show()
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
		const val BASE_URL = "https://a1bd-178-185-91-42.ngrok-free.app"
		const val SIGN_UP = "/organizations/signup"
		
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
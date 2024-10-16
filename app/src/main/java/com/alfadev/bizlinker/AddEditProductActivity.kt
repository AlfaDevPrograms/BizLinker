package com.alfadev.bizlinker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.alfadev.bizlinker.MainActivity.Companion.sharedPreferences
import com.alfadev.bizlinker.databinding.ActivityAddEditProductBinding
import com.bumptech.glide.Glide
import java.io.File

class AddEditProductActivity: AppCompatActivity() {
	companion object {
		var editProductItem: ProductItem? = null
	}
	//Биндинг
	private lateinit var binding: ActivityAddEditProductBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		sharedPreferences = getSharedPreferences(getString(R.string.shared_pref), MODE_PRIVATE)
		MainActivity.setTheme(this) //Создание самого проекта и привязки!
		super.onCreate(savedInstanceState)
		binding = ActivityAddEditProductBinding.inflate(layoutInflater)
		setContentView(binding.root)
		if(editProductItem != null) {
			binding.addEditProductHeaderTxt.text = getString(R.string.edit_product)
			binding.addEditProductNameTIET.setText(editProductItem!!.name)
			binding.addEditProductPriceTIET.setText(editProductItem!!.price.toString())
			if(editProductItem!!.fileUrl != null) {
				binding.addEditProductPhotoTIET.setText(editProductItem!!.fileUrl.toString())
			}
			binding.addEditProductCountTIET.setText(editProductItem!!.count.toString())
			binding.addEditProductDescriptionTIET.setText(editProductItem!!.description)
		}
		else {
			binding.addEditProductHeaderTxt.text = getString(R.string.add_product)
			binding.addEditProductNameTIET.setText("")
			binding.addEditProductPriceTIET.setText("")
			binding.addEditProductPhotoTIET.setText("")
			binding.addEditProductCountTIET.setText("")
			binding.addEditProductDescriptionTIET.setText("")
		}
		binding.backToProductsFromAddEditProduct.setOnClickListener {
			editProductItem = null
			finish()
		}
		binding.cardviewAddEditProductSave.setOnClickListener {
			if(binding.addEditProductNameTIET.text!!.isEmpty()) {
				binding.addEditProductNameTIL.error = getString(R.string.error_input)
			}
			if(binding.addEditProductPriceTIET.text!!.isEmpty()) {
				binding.addEditProductPriceTIL.error = getString(R.string.error_input)
			}
			if(binding.addEditProductCountTIET.text!!.isEmpty()) {
				binding.addEditProductCountTIL.error = getString(R.string.error_input)
			}
			if(binding.addEditProductDescriptionTIET.text!!.isEmpty()) {
				binding.addEditProductDescriptionTIL.error = getString(R.string.error_input)
			}
			if(binding.addEditProductNameTIET.text!!.isNotEmpty() && binding.addEditProductPriceTIET.text!!.isNotEmpty() && binding.addEditProductCountTIET.text!!.isNotEmpty() && binding.addEditProductDescriptionTIET.text!!.isNotEmpty()) {
				editProductItem = null
				finish()
			}
		}
		val someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if(result.resultCode == Activity.RESULT_OK) {
				val data: Intent? = result.data
				val selectedImageUri: Uri? = data!!.data
				val finalFile = File(getRealPathFromURI(selectedImageUri)!!)
				binding.addEditProductPhotoTIET.setText(finalFile.toString())
			}
		}
		binding.cardviewAddEditProductPickPhoto.setOnClickListener {
			if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100)
			}
			else {
				val intent = Intent(Intent.ACTION_PICK)
				intent.type = "image/*"
				someActivityResultLauncher.launch(intent)
			}
		}
		binding.addEditProductPhotoTIET.doAfterTextChanged {
			try {
				if(binding.addEditProductPhotoTIET.text!!.isNotEmpty()) {
					if(binding.addEditProductPhotoTIET.text!![0] == '/') {
						Glide.with(this).load(File(binding.addEditProductPhotoTIET.text!!.toString())).into(binding.addEditProductPhoto)
					}
					else {
						Glide.with(this).load(binding.addEditProductPhotoTIET.text!!.toString()).into(binding.addEditProductPhoto)
					}
				}
			}
			catch(_: Exception) {
			}
		}
		binding.cardviewAddEditProductPhoto.setOnClickListener {
			binding.cardviewAddEditProductPickPhoto.performClick()
		}
		binding.addEditProductNameTIET.doAfterTextChanged {
			if(it!!.isNotEmpty()) {
				binding.addEditProductNameTIL.error = null
			}
		}
		binding.addEditProductPriceTIET.doAfterTextChanged {
			if(it!!.isNotEmpty()) {
				binding.addEditProductPriceTIL.error = null
			}
		}
		binding.addEditProductCountTIET.doAfterTextChanged {
			if(it!!.isNotEmpty()) {
				binding.addEditProductCountTIL.error = null
			}
		}
		binding.addEditProductDescriptionTIET.doAfterTextChanged {
			if(it!!.isNotEmpty()) {
				binding.addEditProductDescriptionTIL.error = null
			}
		}
	}
	@SuppressLint("Recycle")
	fun getRealPathFromURI(uri: Uri?): String? {
		val cursor = contentResolver.query(uri!!, null, null, null, null)
		cursor!!.moveToFirst()
		val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
		return cursor.getString(idx)
	}
}
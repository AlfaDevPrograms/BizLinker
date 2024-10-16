package com.alfadev.bizlinker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class SearchAdapter(private val searchList: ArrayList<OrganizationItem>): RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
	inner class SearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		val userNameTxt: TextView = itemView.findViewById(R.id.searchNameTxt)
		val descriptionTxt: TextView = itemView.findViewById(R.id.searchDescriptionTxt)
		val logoTxt: TextView = itemView.findViewById(R.id.searchLogoTxt)
		val logo: MaterialCardView = itemView.findViewById(R.id.cardviewIconSearch)
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
		val view: View = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
		return SearchViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
		val searchItem = searchList[position]
		holder.userNameTxt.text = searchItem.name
		holder.descriptionTxt.text = searchItem.innNumber
		holder.logoTxt.text = searchItem.name.substring(0, 2)
	}
	
	override fun getItemCount(): Int {
		return searchList.size
	}
}
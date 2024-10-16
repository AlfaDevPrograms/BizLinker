package com.alfadev.bizlinker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alfadev.bizlinker.ChatAdapter.OnItemClickListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InvoiceAdapter(private val invoicesList: ArrayList<InvoiceItem>, private val itemClickListener: OnItemClickListener): RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder>() {
	interface OnItemClickListener {
		fun onItemClick(position: Int)
	}
	
	inner class InvoiceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		val id: TextView = itemView.findViewById(R.id.invoiceNumberTxt)
		val createAt: TextView = itemView.findViewById(R.id.invoiceDateTxt)
		val organization: TextView = itemView.findViewById(R.id.invoiceOrganizationNameTxt)
		val summaryPrice: TextView = itemView.findViewById(R.id.invoiceSummTxt)
		
		init {
			itemView.setOnClickListener {
				val position = bindingAdapterPosition
				if(position != RecyclerView.NO_POSITION) {
					itemClickListener.onItemClick(position)
				}
			}
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
		val view: View = LayoutInflater.from(parent.context).inflate(R.layout.invoice_item, parent, false)
		return InvoiceViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
		val invoiceItem = invoicesList[position]
		holder.id.text = invoiceItem.id.toString()
		val date = Date(invoiceItem.createAt)
		val sdf = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
		val formattedDate = sdf.format(date)
		holder.createAt.text = formattedDate
		holder.organization.text = invoiceItem.organization.name
		holder.summaryPrice.text = invoiceItem.summaryPrice.toString()
	}
	
	override fun getItemCount(): Int {
		return invoicesList.size
	}
}
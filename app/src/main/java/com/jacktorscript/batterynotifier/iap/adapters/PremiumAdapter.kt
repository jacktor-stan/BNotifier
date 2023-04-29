package com.jacktorscript.batterynotifier.iap.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import com.jacktorscript.batterynotifier.R
import com.jacktorscript.batterynotifier.core.PrefsConfig
import com.jacktorscript.batterynotifier.iap.interfaces.RecycleViewInterface


class PremiumAdapter(
    var context: Context,
    private var productDetailsList: List<ProductDetails>,
    var recycleViewInterface: RecycleViewInterface, private var prefsConfig: PrefsConfig
) :
    RecyclerView.Adapter<PremiumAdapter.BuyPremiumViewHolder?>() {
    //var TAG = "TestINAPP"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyPremiumViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.premium_item, parent, false)
        return BuyPremiumViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuyPremiumViewHolder, position: Int) {
        val currentItem = productDetailsList[position]

        prefsConfig = PrefsConfig(context)
        if (prefsConfig.getPremium() == 0) {
            holder.txtPremiumTitle.text = currentItem.name
        } else {
            holder.txtPremiumTitle.text = context.getString(R.string.already_purchased)
        }

        holder.txtPremiumPrice.text = currentItem.oneTimePurchaseOfferDetails?.formattedPrice

    }

    override fun getItemCount(): Int {
        return productDetailsList.size
    }

    inner class BuyPremiumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtPremiumTitle: TextView
        var txtPremiumPrice: TextView

        init {
            txtPremiumTitle = itemView.findViewById(R.id.product_name)
            txtPremiumPrice = itemView.findViewById(R.id.product_price)
            itemView.setOnClickListener {
                recycleViewInterface.onItemClick(bindingAdapterPosition)
            }
        }
    }
}

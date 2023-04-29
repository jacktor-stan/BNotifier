package com.jacktorscript.batterynotifier.ui.about

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.jacktorscript.batterynotifier.R
import com.squareup.picasso.Picasso


class AdapterTeam(
    private val context: Context,
    private val list: ArrayList<DataModel>,
    private val recyclerViewClickListener: RecyclerViewClickListener
) : RecyclerView.Adapter<AdapterTeam.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView = itemView.findViewById<View>(R.id.name) as TextView
        var username: TextView = itemView.findViewById<View>(R.id.username) as TextView
        var status: TextView = itemView.findViewById<View>(R.id.project_status) as TextView
        var iv: ImageView = itemView.findViewById<View>(R.id.iv) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.about_team_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        Picasso.get().load(data.getimgURLs()).into(holder.iv)
        holder.name.text = data.getNames()

        holder.username.text =
            holder.username.context.getString(R.string.username, data.getUsernames())
        val projectStatus = data.getStatus()

        if (projectStatus == "1") {
            holder.status.text = holder.status.context.getString(R.string.project_status)
        } else {
            holder.status.text = ""
        }

        holder.itemView.setOnClickListener {
            recyclerViewClickListener.onItemClicked(data)
        }
    }
}
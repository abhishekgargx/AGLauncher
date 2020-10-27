package com.abhishek.aglauncher

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.abhishek.aglauncher.databinding.ItemAppBinding

/**
 * Created by Abhishek Garg on 26/10/20 - https://www.linkedin.com/in/abhishekgarg727/
 */
public class AppAdapter(var context: Context, var appList: List<AppObject>, var appAdapterClicks: AppAdapterClicks) :
    RecyclerView.Adapter<AppAdapter.MyViewHolder>() {

    class MyViewHolder(itemAppBinding: ItemAppBinding) :
        RecyclerView.ViewHolder(itemAppBinding.root) {
        val mItemAppBinding: ItemAppBinding = itemAppBinding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemAppBinding: ItemAppBinding =
            DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_app, parent, false)
        return MyViewHolder(itemAppBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val appObject = appList[position]
        holder.mItemAppBinding.icon.setImageDrawable(appObject.image)
        holder.mItemAppBinding.label.text = appObject.name
        holder.mItemAppBinding.appItemLayout.setOnClickListener {
            appAdapterClicks.onItemClick(appObject)
        }
        holder.mItemAppBinding.appItemLayout.setOnLongClickListener {
            appAdapterClicks.onItemLongClick(appObject)
        }
    }

    override fun getItemCount(): Int {
        return appList.size
    }

    interface AppAdapterClicks{
        fun onItemClick(appObject: AppObject)
        fun onItemLongClick(appObject: AppObject) : Boolean
    }

}
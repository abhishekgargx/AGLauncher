package com.abhishek.aglauncher

import android.graphics.drawable.Drawable

/**
 * Created by Abhishek Garg on 26/10/20 - https://www.linkedin.com/in/abhishekgarg727/
 */
data class AppObject(val name: String, val packageName: String, val image: Drawable, val versionCode : String, val versionName: String) : Comparable<AppObject>{
    override fun compareTo(other: AppObject): Int = this.name.compareTo(other.name)

}
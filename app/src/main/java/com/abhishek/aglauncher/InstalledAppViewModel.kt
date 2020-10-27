package com.abhishek.aglauncher

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


/**
 * Created by Abhishek Garg on 26/10/20 - https://www.linkedin.com/in/abhishekgarg727/
 */
class InstalledAppViewModel : ViewModel() {

    val liveAppList: MutableLiveData<MutableList<AppObject>> = MutableLiveData()
    private val installedAppList: MutableList<AppObject> = arrayListOf()
    private val filteredAppList: MutableList<AppObject> = arrayListOf()
    var wallpaper: MutableLiveData<Drawable> = MutableLiveData()

    init {
        liveAppList.value = arrayListOf()
    }

    fun updateLiveAppList(context: Context) {
        updateInstalledAppList(context)
        liveAppList.value = installedAppList
    }

    fun updateInstalledAppList(applicationContext: Context) {
        installedAppList.clear()
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val untreatedAppList: List<ResolveInfo> =
            applicationContext.packageManager.queryIntentActivities(intent, 0)

        for (untreatedApp in untreatedAppList) {
            val appName =
                untreatedApp.activityInfo.loadLabel(applicationContext.packageManager).toString()
            val appPackageName = untreatedApp.activityInfo.packageName.toString()
            val appIcon: Drawable =
                untreatedApp.activityInfo.loadIcon(applicationContext.packageManager)
            val pInfo: PackageInfo =
                applicationContext.getPackageManager().getPackageInfo(appPackageName, 0)
            val versionCode = pInfo.versionCode.toString()
            val versionName = pInfo.versionName
            val appObject = AppObject(appName, appPackageName, appIcon, versionCode, versionName)

            if (!installedAppList.contains(appObject))
                installedAppList.add(appObject)
        }
        installedAppList.sort()
    }

    fun filterListUsingQuery(query: String) {
        filteredAppList.clear()
        for (app in installedAppList) {
            if (app.name.contains(query, true)) {
                filteredAppList.add(app)
            }
        }
        liveAppList.value = filteredAppList
    }

    fun updateCurrentWallpaper(context: Context) {
        try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaper.value = wallpaperManager.drawable
        } catch (e: Exception) {
            wallpaper.value = context.resources.getDrawable(R.drawable.wallpaper)
        }
    }

}
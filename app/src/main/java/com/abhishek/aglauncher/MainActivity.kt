package com.abhishek.aglauncher

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abhishek.aglauncher.AppUtils.Companion.hideKeyboard
import com.abhishek.aglauncher.AppUtils.Companion.launchApp
import com.abhishek.aglauncher.AppUtils.Companion.openAppSettings
import com.abhishek.aglauncher.AppUtils.Companion.uninstallApp
import com.abhishek.aglauncher.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback


class MainActivity : AppCompatActivity(), AppAdapter.AppAdapterClicks {

    private lateinit var dataBinding: ActivityMainBinding;
    private lateinit var appAdapter: AppAdapter
    private lateinit var searchQueryRunnable: Runnable
    private lateinit var searchQueryHandler: Handler
    private var installedAppList: ArrayList<AppObject> = ArrayList()
    private var searchQueryForApi = ""
    private lateinit var viewModel: InstalledAppViewModel
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var appReceiver: BroadcastReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(InstalledAppViewModel::class.java)
        viewModel.updateLiveAppList(applicationContext)
        checkForStoragePermissions()

        initSearchView()
        initRecycleView()
        initDrawer()
        initObservers()
    }

    override fun onResume() {
        super.onResume()
        registerPackageUninstallReciever()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(appReceiver)
    }


    private fun showAppOptions(appObject: AppObject): Boolean {
        val message = "Package name: ${appObject.packageName}\n\n" +
                "Version Code: ${appObject.versionCode}\n\n" +
                "Version Name: ${appObject.versionName}\n"

        DialogHelper.threeBtnDialog(this,
            appObject.name,
            message,
            "Settings",
            "Uninstall",
            "Cancel",
            object : DialogHelper.ThreeBtnDialogListener {
                override fun onPositiveBtnPress() {
                    openAppSettings(appObject.packageName, this@MainActivity)
                }

                override fun onNegativeBtnPress() {
                    uninstallApp(appObject.packageName, this@MainActivity)
                }

                override fun onNeutralBtnPress() {

                }
            })
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun registerPackageUninstallReciever() {
        appReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val packageName: String? = intent.data?.getEncodedSchemeSpecificPart()
                when (intent.action) {
                    Intent.ACTION_PACKAGE_REMOVED -> {
                        showToast("App Removed: ${packageName ?: ""}")
                    }
                    Intent.ACTION_PACKAGE_ADDED -> {
                        showToast("App Added: ${packageName ?: ""}")
                    }
                }
                viewModel.updateLiveAppList(applicationContext)
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addDataScheme("package")
        registerReceiver(appReceiver, intentFilter)
    }

    private fun checkForStoragePermissions() {
        permissionHelper = PermissionHelper(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            .shouldAskAgainIfPermissionDenied(true)
            .setCallback(object : PermissionHelper.Callback {
                override fun success() {
                    viewModel.updateCurrentWallpaper(this@MainActivity)
                }

                override fun failure() {
                }
            })
        permissionHelper.askForPermissions()
    }

    private fun changeCurrentWallpaper(drawable: Drawable) {
        dataBinding.parentLayout.background = drawable
    }

    private fun initObservers() {
        viewModel.liveAppList.observe(this, Observer {
            installedAppList.clear()
            installedAppList.addAll(it)
            appAdapter.notifyDataSetChanged()
        })
        viewModel.wallpaper.observe(this, Observer {
            changeCurrentWallpaper(it)
        })
    }

    private fun initSearchView() {
        dataBinding.searchView.visibility = View.GONE
        searchQueryHandler = Handler()
        searchQueryRunnable = Runnable {
            performSearchOverQuery(searchQueryForApi)
            searchQueryHandler.removeCallbacks(searchQueryRunnable)
        }
        setSearchListener()
    }

    private fun performSearchOverQuery(query: String) {
        viewModel.filterListUsingQuery(query)
    }

    private fun setSearchListener() {
        dataBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchQueryHandler.removeCallbacks(searchQueryRunnable)
                performSearchOverQuery(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchQueryForApi = newText
                searchQueryHandler.removeCallbacks(searchQueryRunnable)
                searchQueryHandler.postDelayed(searchQueryRunnable, 1000)
                return false
            }
        })
    }

    private fun initRecycleView() {
        appAdapter = AppAdapter(applicationContext, installedAppList, this)
        dataBinding.recycleView.layoutManager = GridLayoutManager(this, 4)
        dataBinding.recycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                    hideKeyboard(this@MainActivity)
            }
        })
        dataBinding.recycleView.adapter = appAdapter
    }

    private fun initDrawer() {
        val bottomSheetBehavior = BottomSheetBehavior.from(dataBinding.bottomSheet)
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.peekHeight = 350
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if ((newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_DRAGGING)) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset > 0) {
                    dataBinding.arrowImage.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24))
                    dataBinding.searchView.animate().alpha(1.0f)
                    dataBinding.searchView.visibility = View.VISIBLE
                } else {
                    dataBinding.arrowImage.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24))
                    dataBinding.searchView.animate().alpha(0.0f)
                    dataBinding.searchView.visibility = View.GONE
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper.handlePermissionResult(requestCode, permissions, grantResults)
    }

    override fun onItemClick(appObject: AppObject) {
        launchApp(appObject.packageName, applicationContext)
    }

    override fun onItemLongClick(appObject: AppObject): Boolean {
        showAppOptions(appObject)
        return true
    }


}
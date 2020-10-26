package com.abhishek.aglauncher

import android.Manifest
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.abhishek.aglauncher.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback


class MainActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivityMainBinding;
    private lateinit var appAdapter: AppAdapter
    private lateinit var searchQueryRunnable: Runnable
    private lateinit var searchQueryHandler: Handler
    private var installedAppList: ArrayList<AppObject> = ArrayList()
    private var searchQueryForApi = ""
    private lateinit var viewModel: InstalledAppViewModel
    private lateinit var permissionHelper: PermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(InstalledAppViewModel::class.java)
        viewModel.updateInstalledAppList(applicationContext)
        checkForStoragePermissions()
        initSearchView()
        initRecycleView()
        initDrawer()
        initObservers()
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
        appAdapter = AppAdapter(applicationContext, installedAppList)
        dataBinding.recycleView.layoutManager = GridLayoutManager(this, 4)
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


}
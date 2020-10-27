package com.abhishek.aglauncher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager


/**
 * Created by Abhishek Garg on 27/10/20 - https://www.linkedin.com/in/abhishekgarg727/
 */
class AppUtils {
    companion object {
        fun launchApp(packageName: String, context: Context) {
            val intent: Intent? = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
            }
        }

        fun openAppSettings(packageName: String, context: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }

        fun uninstallApp(packageName: String, context: Activity) {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:$packageName")
            context.startActivity(intent)
        }

        fun hideKeyboard(activity: Activity?) {
            if (activity == null) {
                return
            }
            val imm: InputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view: View? = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }

    }
}
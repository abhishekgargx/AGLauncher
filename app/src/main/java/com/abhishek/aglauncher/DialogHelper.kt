package com.abhishek.aglauncher

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

/**
 * Created by Abhishek Garg on 27/10/20 - https://www.linkedin.com/in/abhishekgarg727/
 */
class DialogHelper {
    companion object {
        fun singleBtnDialog(
            context: Context?,
            title: String = "",
            message: String = "",
            singleBtnDialogListener: SingleBtnDialogListener
        ) {
            if (context != null) {
                val builder = AlertDialog.Builder(context)
                builder.setCancelable(false)
                builder.setTitle(title)
                builder.setMessage(message)
                builder.setPositiveButton(
                    "Okay"
                ) { dialog: DialogInterface?, which: Int -> singleBtnDialogListener.onOkayPress() }
                builder.create().show()
            }
        }

        fun doubleBtnDialog(
            context: Context?,
            title: String = "",
            message: String = "",
            positiveBtnText: String = "Okay",
            negativeBtnText: String = "Cancel",
            doubleBtnDialogListener: DoubleBtnDialogListener
        ) {
            if (context != null) {
                val builder = AlertDialog.Builder(context)
                builder.setCancelable(false)
                builder.setTitle(title)
                builder.setMessage(message)
                builder.setPositiveButton(
                    positiveBtnText
                ) { dialog: DialogInterface?, which: Int -> doubleBtnDialogListener.onPositiveBtnPress() }

                builder.setNegativeButton(
                    negativeBtnText
                ) { dialog: DialogInterface?, which: Int -> doubleBtnDialogListener.onNegativeBtnPress() }
                builder.create().show()
            }
        }

        fun threeBtnDialog(
            context: Context?,
            title: String = "",
            message: String = "",
            positiveBtnText: String = "Yes",
            negativeBtnText: String = "No",
            neutralBtnText: String = "Cancel",
            threeBtnDialogListener: ThreeBtnDialogListener
        ) {
            if (context != null) {
                val builder = AlertDialog.Builder(context)
                builder.setCancelable(false)
                builder.setTitle(title)
                builder.setMessage(message)
                builder.setPositiveButton(
                    positiveBtnText
                ) { dialog: DialogInterface?, which: Int -> threeBtnDialogListener.onPositiveBtnPress() }

                builder.setNegativeButton(
                    negativeBtnText
                ) { dialog: DialogInterface?, which: Int -> threeBtnDialogListener.onNegativeBtnPress() } 
                
                builder.setNeutralButton(
                    neutralBtnText
                ) { dialog: DialogInterface?, which: Int -> threeBtnDialogListener.onNeutralBtnPress() }
                builder.create().show()
            }
        }
    }

    interface SingleBtnDialogListener {
        fun onOkayPress()
    }

    interface DoubleBtnDialogListener {
        fun onPositiveBtnPress()
        fun onNegativeBtnPress()
    }

    interface ThreeBtnDialogListener {
        fun onPositiveBtnPress()
        fun onNegativeBtnPress()
        fun onNeutralBtnPress()
    }
}
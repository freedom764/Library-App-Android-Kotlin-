package edu.skku.cs.final_library

import android.app.Activity
import android.app.AlertDialog


class LoadingDialog internal constructor(var activity: Activity) {
    val inflater = activity.layoutInflater
    val dialogView = inflater.inflate(R.layout.custom_dialog,null)
    val progressDialog = AlertDialog.Builder(activity).setView(dialogView)



    val dialog = progressDialog.create()


    fun startLoadingDialog() {

        progressDialog.setView(dialogView)


        dialog.setCancelable(false)
        dialog.show()
    }

    fun dismissDialog() {
        dialog.dismiss()
    }
}

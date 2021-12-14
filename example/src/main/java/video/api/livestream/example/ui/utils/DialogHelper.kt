package video.api.livestream.example.ui.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import video.api.livestream.app.R

object DialogHelper {
    fun showAlertDialog(context: Context, title: String, message: String = "") {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
            .show()
    }

    fun showPermissionAlertDialog(context: Context, afterPositiveButton: () -> Unit = {}) {
        AlertDialog.Builder(context)
            .setTitle(R.string.permissions)
            .setMessage(R.string.permission_not_granted)
            .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                afterPositiveButton()
            }
            .show()
    }
}